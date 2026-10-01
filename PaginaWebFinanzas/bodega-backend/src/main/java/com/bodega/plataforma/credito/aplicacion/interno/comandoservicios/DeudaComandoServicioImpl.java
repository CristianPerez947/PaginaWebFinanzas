package com.bodega.plataforma.credito.aplicacion.interno.comandoservicios;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.compartido.dominio.servicios.Redondeo;
import com.bodega.plataforma.compartido.dominio.servicios.RelojSistema;
import com.bodega.plataforma.credito.aplicacion.comandoservicios.DeudaComandoServicio;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.comandos.CondonarDeudaClienteComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.CondonarObligacionComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarPagoCicloComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarPagoCuotaComando;
import com.bodega.plataforma.credito.dominio.modelo.entidades.Pago;
import com.bodega.plataforma.credito.dominio.modelo.eventos.OperacionCreditoEvento;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import com.bodega.plataforma.credito.dominio.repositorios.ObligacionRepositorio;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de aplicación para pagos y condonaciones. No hay pagos parciales: un pago cancela una obligación
 * completa a fin de mes o una cuota completa, siempre con el desglose mora, interés compensatorio y capital.
 */
@Service
@Transactional
public class DeudaComandoServicioImpl implements DeudaComandoServicio {

    private final ObligacionRepositorio obligacionRepositorio;
    private final RelojSistema reloj;
    private final ApplicationEventPublisher publicadorEventos;

    public DeudaComandoServicioImpl(ObligacionRepositorio obligacionRepositorio, RelojSistema reloj,
                                    ApplicationEventPublisher publicadorEventos) {
        this.obligacionRepositorio = obligacionRepositorio;
        this.reloj = reloj;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public Resultado<ResumenPago, ErrorAplicacion> manejar(RegistrarPagoCicloComando c) {
        var fechaPago = resolverFechaPago(c.fechaPagoReal());
        if (fechaPago.isEmpty())
            return Resultado.fallo(ErrorAplicacion.validacion("fecha de pago", "La fecha de pago no puede ser futura"));
        var obligaciones = obligacionRepositorio.buscarPendientesPorCliente(c.tiendaId(), c.clienteId()).stream()
                .filter(o -> o.getModalidad() == Modalidad.FIN_DE_MES && o.getFechaPago().equals(c.fechaPagoCiclo()))
                .toList();
        if (obligaciones.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Obligación pendiente",
                    "fecha de pago %s".formatted(c.fechaPagoCiclo())));
        var pagos = new ArrayList<Pago>();
        try {
            for (var o : obligaciones) {
                pagos.add(o.pagarFinDeMes(fechaPago.get()));
                obligacionRepositorio.guardar(o);
            }
        } catch (IllegalStateException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("pago no válido", e.getMessage()));
        }
        return Resultado.exito(registrar(c.tiendaId(), c.clienteId(), obligaciones.get(0).getId(), fechaPago.get(),
                pagos, "Pago total del ciclo con vencimiento %s".formatted(c.fechaPagoCiclo())));
    }

    @Override
    public Resultado<ResumenPago, ErrorAplicacion> manejar(RegistrarPagoCuotaComando c) {
        var fechaPago = resolverFechaPago(c.fechaPagoReal());
        if (fechaPago.isEmpty())
            return Resultado.fallo(ErrorAplicacion.validacion("fecha de pago", "La fecha de pago no puede ser futura"));
        var obligacion = obligacionRepositorio.buscarPorIdYTienda(c.obligacionId(), c.tiendaId());
        if (obligacion.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Obligación", c.obligacionId().toString()));
        Pago pago;
        try {
            pago = obligacion.get().pagarCuota(c.numeroCuota(), fechaPago.get());
        } catch (IllegalStateException e) {
            return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("pago no válido", e.getMessage()));
        }
        obligacionRepositorio.guardar(obligacion.get());
        return Resultado.exito(registrar(c.tiendaId(), obligacion.get().getClienteId(), c.obligacionId(), fechaPago.get(),
                List.of(pago), "Pago de la cuota %d de la obligación %d".formatted(c.numeroCuota(), c.obligacionId())));
    }

    @Override
    public Resultado<ResumenCondonacion, ErrorAplicacion> manejar(CondonarObligacionComando c) {
        var obligacion = obligacionRepositorio.buscarPorIdYTienda(c.obligacionId(), c.tiendaId());
        if (obligacion.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Obligación", c.obligacionId().toString()));
        try {
            return Resultado.exito(condonar(List.of(obligacion.get()), c.motivo()));
        } catch (IllegalStateException e) {
            return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("condonación no válida", e.getMessage()));
        }
    }

    @Override
    public Resultado<ResumenCondonacion, ErrorAplicacion> manejar(CondonarDeudaClienteComando c) {
        var pendientes = obligacionRepositorio.buscarPendientesPorCliente(c.tiendaId(), c.clienteId());
        if (pendientes.isEmpty())
            return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("sin deuda", "El cliente no tiene deuda pendiente"));
        return Resultado.exito(condonar(pendientes, c.motivo()));
    }

    private ResumenCondonacion condonar(List<Obligacion> obligaciones, String motivo) {
        var hoy = reloj.hoy();
        var monto = BigDecimal.ZERO;
        for (var o : obligaciones) {
            monto = monto.add(o.capitalPendiente());
            o.condonar(motivo, hoy);
            obligacionRepositorio.guardar(o);
        }
        var primera = obligaciones.get(0);
        monto = Redondeo.dinero(monto);
        publicadorEventos.publishEvent(new OperacionCreditoEvento(primera.getTiendaId(), primera.getClienteId(),
                "OBLIGACION", primera.getId(), "CONDONAR_DEUDA",
                "Condonación de %d obligación(es) por %s %s. Motivo: %s".formatted(obligaciones.size(),
                        primera.getMoneda(), monto, motivo), monto));
        return new ResumenCondonacion(obligaciones.size(), monto);
    }

    private ResumenPago registrar(Long tiendaId, Long clienteId, Long obligacionId, LocalDate fechaPago,
                                  List<Pago> pagos, String descripcion) {
        var mora = pagos.stream().map(Pago::getMontoMora).reduce(BigDecimal.ZERO, BigDecimal::add);
        var interes = pagos.stream().map(Pago::getMontoInteres).reduce(BigDecimal.ZERO, BigDecimal::add);
        var capital = pagos.stream().map(Pago::getMontoCapital).reduce(BigDecimal.ZERO, BigDecimal::add);
        var resumen = new ResumenPago(pagos.size(), fechaPago, Redondeo.dinero(mora), Redondeo.dinero(interes),
                Redondeo.dinero(capital), Redondeo.dinero(mora.add(interes).add(capital)));
        publicadorEventos.publishEvent(new OperacionCreditoEvento(tiendaId, clienteId, "OBLIGACION", obligacionId,
                "REGISTRAR_PAGO", "%s: total %s (mora %s, interés %s, capital %s), fecha %s".formatted(descripcion,
                resumen.total(), resumen.mora(), resumen.interes(), resumen.capital(), fechaPago), resumen.total()));
        return resumen;
    }

    /** Devuelve la fecha de pago efectiva; vacío si es posterior a hoy. */
    private java.util.Optional<LocalDate> resolverFechaPago(LocalDate solicitada) {
        var hoy = reloj.hoy();
        var fecha = solicitada == null ? hoy : solicitada;
        return fecha.isAfter(hoy) ? java.util.Optional.empty() : java.util.Optional.of(fecha);
    }
}
