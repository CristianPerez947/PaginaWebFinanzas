package com.bodega.plataforma.estadocuenta.dominio.servicios;

import com.bodega.plataforma.compartido.dominio.servicios.Redondeo;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.modelo.entidades.LineaVenta;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import com.bodega.plataforma.estadocuenta.dominio.modelo.entidades.LineaEstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoLinea;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio de dominio que arma las líneas de un estado de cuenta a partir de las obligaciones del cliente.
 * Para el ciclo con fecha de pago P incluye: las compras a fin de mes que vencen en P (con su interés por días),
 * las cuotas que vencen en P, las deudas vencidas de ciclos anteriores y, en ítem aparte, la mora a la fecha de
 * generación. Las compras en cuotas hechas en el ciclo se listan como informativas.
 */
public final class GeneradorEstadoCuenta {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private GeneradorEstadoCuenta() {
    }

    public static List<LineaEstadoCuenta> generarLineas(LocalDate fechaCorte, LocalDate fechaPago,
                                                        List<Obligacion> obligaciones, List<VentaCredito> ventas,
                                                        LocalDate hoy) {
        Map<Long, VentaCredito> ventaPorId = ventas.stream().collect(Collectors.toMap(VentaCredito::getId, Function.identity()));
        var lineas = new ArrayList<LineaEstadoCuenta>();

        for (var o : obligaciones) {
            var productos = describirProductos(o, ventaPorId.get(o.getVentaId()));
            var fechaCompra = o.getFechaCompra().toLocalDate();
            if (o.getModalidad() == Modalidad.FIN_DE_MES) {
                if (!o.estaPendiente() || o.getFechaPago().isAfter(fechaPago)) continue;
                var vencidaAnterior = o.getFechaPago().isBefore(fechaPago);
                lineas.add(new LineaEstadoCuenta(TipoLinea.COMPRA, fechaCompra,
                        (vencidaAnterior ? "Saldo vencido de la compra: " : "Compra: ") + productos,
                        o.getCapital(), true));
                lineas.add(new LineaEstadoCuenta(TipoLinea.INTERES_COMPENSATORIO, fechaCompra,
                        "Intereses por %d día(s), del %s al %s".formatted(o.getDiasGracia(), fechaCompra.format(FECHA),
                                o.getFechaPago().format(FECHA)), o.getInteresCompensatorio(), true));
                agregarMoraFinDeMes(lineas, o, hoy);
            } else {
                if (o.getFechaCorte().equals(fechaCorte))
                    lineas.add(new LineaEstadoCuenta(TipoLinea.COMPRA_EN_CUOTAS, fechaCompra,
                            "Compra en %d cuotas: %s (capital %s; primera cuota el %s)".formatted(o.getNumCuotas(),
                                    productos, o.getCapital(), o.getCuotas().get(0).getFechaVencimiento().format(FECHA)),
                            o.getCapital(), false));
                for (var c : o.getCuotas()) {
                    if (!c.estaPendiente() || c.getFechaVencimiento().isAfter(fechaPago)) continue;
                    var vencidaAnterior = c.getFechaVencimiento().isBefore(fechaPago);
                    lineas.add(new LineaEstadoCuenta(TipoLinea.CUOTA, c.getFechaVencimiento(),
                            "%s %d/%d de la compra del %s".formatted(vencidaAnterior ? "Cuota vencida" : "Cuota",
                                    c.getNumero(), o.getNumCuotas(), fechaCompra.format(FECHA)), c.getCuota(), true));
                    var mora = o.moraCuota(c, hoy);
                    if (mora.signum() > 0)
                        lineas.add(new LineaEstadoCuenta(TipoLinea.MORA, c.getFechaVencimiento(),
                                "Intereses por mora: %d día(s) sobre la cuota %d/%d (vencida el %s)".formatted(
                                        o.diasMoraCuota(c, hoy), c.getNumero(), o.getNumCuotas(),
                                        c.getFechaVencimiento().format(FECHA)), mora, true));
                }
            }
        }
        lineas.sort(Comparator.comparing(LineaEstadoCuenta::getFecha));
        return lineas;
    }

    private static void agregarMoraFinDeMes(List<LineaEstadoCuenta> lineas, Obligacion o, LocalDate hoy) {
        var mora = o.moraFinDeMes(hoy);
        if (mora.signum() > 0)
            lineas.add(new LineaEstadoCuenta(TipoLinea.MORA, o.getFechaPago(),
                    "Intereses por mora: %d día(s) sobre %s (vencida el %s)".formatted(o.diasMoraFinDeMes(hoy),
                            Redondeo.dinero(o.montoFinDeMes()), o.getFechaPago().format(FECHA)), mora, true));
    }

    private static String describirProductos(Obligacion o, VentaCredito venta) {
        if (venta == null) return "venta #%d".formatted(o.getVentaId());
        return venta.getLineas().stream()
                .filter(l -> l.getModalidad() == o.getModalidad()
                        && (o.getModalidad() == Modalidad.FIN_DE_MES || (l.getNumCuotas() != null && l.getNumCuotas() == o.getNumCuotas())))
                .map(GeneradorEstadoCuenta::descripcionLinea)
                .collect(Collectors.joining(", "));
    }

    private static String descripcionLinea(LineaVenta l) {
        return "%s x%s".formatted(l.getNombreProducto(), l.getCantidad().stripTrailingZeros().toPlainString());
    }

    /** Suma lo exigible de un conjunto de líneas (solo las que cuentan en el total). */
    public static BigDecimal total(List<LineaEstadoCuenta> lineas) {
        return Redondeo.dinero(lineas.stream().filter(LineaEstadoCuenta::isIncluidaEnTotal)
                .map(LineaEstadoCuenta::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
