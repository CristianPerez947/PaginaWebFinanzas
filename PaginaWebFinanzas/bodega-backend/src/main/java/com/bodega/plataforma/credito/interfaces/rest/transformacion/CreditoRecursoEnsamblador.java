package com.bodega.plataforma.credito.interfaces.rest.transformacion;

import com.bodega.plataforma.compartido.dominio.servicios.Redondeo;
import com.bodega.plataforma.credito.aplicacion.comandoservicios.VentaCreditoComandoServicio.VentaConstruida;
import com.bodega.plataforma.credito.aplicacion.consultaservicios.DeudaClienteConsultaServicio.DeudaCliente;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarVentaComando;
import com.bodega.plataforma.credito.dominio.modelo.entidades.Cuota;
import com.bodega.plataforma.credito.dominio.modelo.entidades.LineaVenta;
import com.bodega.plataforma.credito.dominio.modelo.entidades.Pago;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.CuotaRecurso;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.DeudaClienteRecurso;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.LineaVentaRecurso;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.ObligacionRecurso;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.PagoRecurso;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.RegistrarVentaSolicitud;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.VentaRecurso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Convierte el modelo de dominio de crédito en recursos REST. La mora y los importes exigibles se calculan
 * con la fecha de consulta indicada (la fecha del sistema).
 */
public final class CreditoRecursoEnsamblador {

    private CreditoRecursoEnsamblador() {
    }

    public static ObligacionRecurso aRecurso(Obligacion o, LocalDate al) {
        var cuotas = o.getCuotas().stream().map(c -> aRecurso(o, c, al)).toList();
        var pagos = o.getPagos().stream().map(CreditoRecursoEnsamblador::aRecurso).toList();
        long diasMora;
        BigDecimal mora;
        BigDecimal exigible;
        if (o.getModalidad() == Modalidad.FIN_DE_MES) {
            diasMora = o.diasMoraFinDeMes(al);
            mora = o.moraFinDeMes(al);
            exigible = o.estaPendiente() ? o.totalFinDeMes(al) : Redondeo.dinero(BigDecimal.ZERO);
        } else {
            var siguiente = o.siguienteCuotaPendiente();
            diasMora = siguiente.map(c -> o.diasMoraCuota(c, al)).orElse(0L);
            mora = cuotas.stream().map(CuotaRecurso::mora).reduce(BigDecimal.ZERO, BigDecimal::add);
            exigible = siguiente.map(c -> c.getCuota().add(o.moraCuota(c, al))).orElse(Redondeo.dinero(BigDecimal.ZERO));
        }
        return new ObligacionRecurso(o.getId(), o.getVentaId(), o.getModalidad(), o.getMoneda(), o.getCapital(),
                o.getInteresCompensatorio(), o.getFechaCompra(), o.getFechaCorte(), o.getFechaPago(),
                o.getDiasGracia(), o.getNumCuotas(), o.getTeaCompensatoria(), o.getTeaMora(), o.getEstado(),
                o.tieneMora(al), diasMora, Redondeo.dinero(mora), exigible, cuotas, pagos, o.getMotivoCondonacion(),
                o.getFechaCondonacion());
    }

    private static CuotaRecurso aRecurso(Obligacion o, Cuota c, LocalDate al) {
        var mora = o.moraCuota(c, al);
        return new CuotaRecurso(c.getNumero(), c.getFechaVencimiento(), c.getCuota(), c.getInteres(),
                c.getAmortizacion(), c.getSaldo(), c.getEstado(), c.getFechaPagoReal(), o.diasMoraCuota(c, al), mora,
                c.estaPendiente() ? c.getCuota().add(mora) : Redondeo.dinero(BigDecimal.ZERO));
    }

    private static PagoRecurso aRecurso(Pago p) {
        return new PagoRecurso(p.getNumeroCuota(), p.getFechaPago(), p.getMontoMora(), p.getMontoInteres(),
                p.getMontoCapital(), p.total());
    }

    public static VentaRecurso aRecurso(VentaCredito v, List<Obligacion> obligaciones, LocalDate al) {
        var lineas = v.getLineas().stream().map(CreditoRecursoEnsamblador::aRecurso).toList();
        return new VentaRecurso(v.getId(), v.getClienteId(), v.getFechaHora(), v.getMoneda(), v.getTipoCambioAplicado(),
                v.getTotal(), lineas, obligaciones == null ? null : obligaciones.stream().map(o -> aRecurso(o, al)).toList());
    }

    public static VentaRecurso aRecurso(VentaConstruida construida, LocalDate al) {
        return aRecurso(construida.venta(), construida.obligaciones(), al);
    }

    private static LineaVentaRecurso aRecurso(LineaVenta l) {
        return new LineaVentaRecurso(l.getProductoId(), l.getNombreProducto(), l.getCantidad(), l.getPrecioUnitario(),
                l.getSubtotal(), l.getModalidad(), l.getNumCuotas());
    }

    public static DeudaClienteRecurso aRecurso(DeudaCliente d) {
        return new DeudaClienteRecurso(d.clienteId(), d.moneda(), d.limiteCredito(), d.capitalPendiente(),
                d.disponible(), d.enMora(), d.totalVencido(), d.fechaConsulta(),
                d.obligaciones().stream().map(o -> aRecurso(o, d.fechaConsulta())).toList());
    }

    public static RegistrarVentaComando aComando(Long tiendaId, RegistrarVentaSolicitud s) {
        if (s.lineas() == null) throw new IllegalArgumentException("La venta debe tener al menos un producto");
        var lineas = s.lineas().stream()
                .map(l -> new RegistrarVentaComando.LineaVentaComando(l.productoId(), l.cantidad(), l.modalidad(), l.numCuotas()))
                .toList();
        return new RegistrarVentaComando(tiendaId, s.clienteId(), lineas);
    }
}
