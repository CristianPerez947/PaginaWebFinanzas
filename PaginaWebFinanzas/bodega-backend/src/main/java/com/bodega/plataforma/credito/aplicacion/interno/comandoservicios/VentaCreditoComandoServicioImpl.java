package com.bodega.plataforma.credito.aplicacion.interno.comandoservicios;

import com.bodega.plataforma.catalogo.interfaces.acl.CatalogoContextoFachada;
import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada;
import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada.ClienteParaCredito;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.compartido.dominio.servicios.Redondeo;
import com.bodega.plataforma.compartido.dominio.servicios.RelojSistema;
import com.bodega.plataforma.credito.aplicacion.comandoservicios.VentaCreditoComandoServicio;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarVentaComando;
import com.bodega.plataforma.credito.dominio.modelo.entidades.LineaVenta;
import com.bodega.plataforma.credito.dominio.modelo.eventos.OperacionCreditoEvento;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.TasaAnual;
import com.bodega.plataforma.credito.dominio.repositorios.ObligacionRepositorio;
import com.bodega.plataforma.credito.dominio.repositorios.VentaCreditoRepositorio;
import com.bodega.plataforma.credito.dominio.servicios.CalculadoraFinanciera;
import com.bodega.plataforma.tiendas.interfaces.acl.TiendasContextoFachada;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Servicio de aplicación que registra ventas fiadas. Aplica las reglas del enunciado: sin compras sobre el
 * límite de crédito, sin crédito nuevo a clientes con mora, modalidad por línea y plan de pagos francés.
 */
@Service
@Transactional
public class VentaCreditoComandoServicioImpl implements VentaCreditoComandoServicio {

    private final ClientesContextoFachada clientesFachada;
    private final CatalogoContextoFachada catalogoFachada;
    private final TiendasContextoFachada tiendasFachada;
    private final ObligacionRepositorio obligacionRepositorio;
    private final VentaCreditoRepositorio ventaRepositorio;
    private final RelojSistema reloj;
    private final ApplicationEventPublisher publicadorEventos;

    public VentaCreditoComandoServicioImpl(ClientesContextoFachada clientesFachada, CatalogoContextoFachada catalogoFachada,
                                           TiendasContextoFachada tiendasFachada,
                                           ObligacionRepositorio obligacionRepositorio,
                                           VentaCreditoRepositorio ventaRepositorio, RelojSistema reloj,
                                           ApplicationEventPublisher publicadorEventos) {
        this.clientesFachada = clientesFachada;
        this.catalogoFachada = catalogoFachada;
        this.tiendasFachada = tiendasFachada;
        this.obligacionRepositorio = obligacionRepositorio;
        this.ventaRepositorio = ventaRepositorio;
        this.reloj = reloj;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public Resultado<VentaConstruida, ErrorAplicacion> previsualizar(RegistrarVentaComando comando) {
        return construir(comando);
    }

    @Override
    public Resultado<VentaConstruida, ErrorAplicacion> manejar(RegistrarVentaComando comando) {
        return construir(comando).encadenar(construida -> {
            var venta = ventaRepositorio.guardar(construida.venta());
            var obligaciones = new ArrayList<Obligacion>();
            for (var obligacion : construida.obligaciones()) {
                obligacion.setVentaId(venta.getId());
                obligaciones.add(obligacionRepositorio.guardar(obligacion));
            }
            publicadorEventos.publishEvent(new OperacionCreditoEvento(venta.getTiendaId(), venta.getClienteId(), "VENTA",
                    venta.getId(), "REGISTRAR_VENTA", "Venta de %s %s registrada (%d producto(s))".formatted(
                    venta.getMoneda(), venta.getTotal(), venta.getLineas().size()), venta.getTotal()));
            return Resultado.exito(new VentaConstruida(venta, obligaciones));
        });
    }

    private Resultado<VentaConstruida, ErrorAplicacion> construir(RegistrarVentaComando comando) {
        var clienteOpt = clientesFachada.obtenerCliente(comando.tiendaId(), comando.clienteId());
        if (clienteOpt.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Cliente", comando.clienteId().toString()));
        var cliente = clienteOpt.get();
        if (!cliente.activo())
            return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("cliente dado de baja",
                    "%s está dado de baja y no puede recibir crédito".formatted(cliente.nombreCompleto())));

        var ahora = reloj.ahora();
        var pendientes = obligacionRepositorio.buscarPendientesPorCliente(comando.tiendaId(), comando.clienteId());
        if (pendientes.stream().anyMatch(o -> o.tieneMora(ahora.toLocalDate())))
            return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("cliente con mora",
                    "%s tiene deuda vencida. Debe regularizar su pago antes de recibir nuevo crédito"
                            .formatted(cliente.nombreCompleto())));

        BigDecimal tipoCambio = null;
        if ("USD".equals(cliente.moneda())) {
            var cambio = tiendasFachada.obtenerTipoCambioUsd(comando.tiendaId());
            if (cambio.isEmpty())
                return Resultado.fallo(ErrorAplicacion.noEncontrado("Tienda", comando.tiendaId().toString()));
            tipoCambio = cambio.get();
        }

        var lineas = new ArrayList<LineaVenta>();
        for (var l : comando.lineas()) {
            var producto = catalogoFachada.obtenerProductoActivo(comando.tiendaId(), l.productoId());
            if (producto.isEmpty())
                return Resultado.fallo(ErrorAplicacion.noEncontrado("Producto", l.productoId().toString()));
            var p = producto.get();
            if (l.modalidad() == Modalidad.CUOTAS) {
                if (!p.permiteCuotas())
                    return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("producto sin cuotas",
                            "'%s' solo puede venderse a fin de mes o al contado".formatted(p.nombre())));
                if (l.numCuotas() > cliente.maxMeses())
                    return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("plazo excedido",
                            "%s puede recibir crédito hasta en %d meses".formatted(cliente.nombreCompleto(), cliente.maxMeses())));
            }
            var precioSoles = l.modalidad() == Modalidad.CONTADO ? p.precioContado() : p.precioCredito();
            var precioUnitario = tipoCambio == null
                    ? Redondeo.dinero(precioSoles)
                    : precioSoles.divide(tipoCambio, 2, RoundingMode.HALF_UP);
            var subtotal = Redondeo.dinero(l.cantidad().multiply(precioUnitario));
            lineas.add(new LineaVenta(p.id(), p.nombre(), l.cantidad(), precioUnitario, subtotal, l.modalidad(), l.numCuotas()));
        }

        var totalCredito = lineas.stream().filter(l -> l.getModalidad() != Modalidad.CONTADO)
                .map(LineaVenta::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        var deudaPendiente = pendientes.stream().map(Obligacion::capitalPendiente).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (deudaPendiente.add(totalCredito).compareTo(cliente.limiteCredito()) > 0)
            return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("límite de crédito excedido",
                    "Deuda pendiente %s %s + compra %s %s supera el límite de %s %s".formatted(
                            cliente.moneda(), Redondeo.dinero(deudaPendiente), cliente.moneda(), Redondeo.dinero(totalCredito),
                            cliente.moneda(), cliente.limiteCredito())));

        var venta = new VentaCredito(comando.tiendaId(), comando.clienteId(), ahora, cliente.moneda(), tipoCambio, lineas);
        return Resultado.exito(new VentaConstruida(venta, generarObligaciones(cliente, lineas, ahora)));
    }

    private List<Obligacion> generarObligaciones(ClienteParaCredito cliente, List<LineaVenta> lineas,
                                                 java.time.LocalDateTime ahora) {
        var tea = CalculadoraFinanciera.tasaEfectivaAnual(aTasa(cliente.tipoTasa(), cliente.tasaCompensatoria(),
                cliente.capitalizacion()));
        var teaMora = CalculadoraFinanciera.tasaEfectivaAnual(aTasa(cliente.tipoTasaMora(), cliente.tasaMora(),
                cliente.capitalizacionMora()));
        var ciclo = CalculadoraFinanciera.calcularCiclo(ahora, cliente.diaCorte(), cliente.horaCorte(), cliente.diaPago());

        var obligaciones = new ArrayList<Obligacion>();
        var finDeMes = lineas.stream().filter(l -> l.getModalidad() == Modalidad.FIN_DE_MES)
                .map(LineaVenta::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (finDeMes.signum() > 0)
            obligaciones.add(Obligacion.finDeMes(cliente.tiendaId(), cliente.id(), cliente.moneda(), finDeMes, ahora,
                    ciclo, tea, teaMora));

        Map<Integer, BigDecimal> porNumeroDeCuotas = new TreeMap<>();
        lineas.stream().filter(l -> l.getModalidad() == Modalidad.CUOTAS)
                .forEach(l -> porNumeroDeCuotas.merge(l.getNumCuotas(), l.getSubtotal(), BigDecimal::add));
        porNumeroDeCuotas.forEach((n, monto) -> obligaciones.add(Obligacion.enCuotas(cliente.tiendaId(), cliente.id(),
                cliente.moneda(), monto, ahora, ciclo, tea, teaMora, n)));
        return obligaciones;
    }

    static TasaAnual aTasa(String tipo, BigDecimal porcentaje, String capitalizacion) {
        var nominal = "NOMINAL".equals(tipo);
        var dias = "DIARIA".equals(capitalizacion) ? 1 : 30;
        return new TasaAnual(nominal, porcentaje, dias);
    }
}
