package com.bodega.plataforma.analitica.dominio.modelo.vistas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Vistas de lectura del contexto de analítica. Todos los importes están expresados en soles (PEN); los
 * créditos en dólares se convierten con el tipo de cambio de la tienda.
 */
public final class VistasAnalitica {

    private VistasAnalitica() {
    }

    public record PuntoSerie(String etiqueta, BigDecimal valor) {
    }

    public record FilaDeudor(Long clienteId, String cliente, String dni, String telefono, BigDecimal capitalPendiente,
                             BigDecimal limiteCredito, BigDecimal importeExigible, boolean enMora) {
    }

    public record FilaMoroso(Long clienteId, String cliente, String telefono, long diasMoraMaximo,
                             BigDecimal importeVencido, BigDecimal moraAcumulada) {
    }

    public record FilaProductoVendido(String producto, BigDecimal cantidad, BigDecimal monto) {
    }

    public record FilaInteresMensual(String mes, BigDecimal interesesCompensatorios, BigDecimal interesesMora,
                                     BigDecimal total) {
    }

    public record ProximoVencimiento(Long clienteId, String cliente, LocalDate fecha, BigDecimal importe,
                                     String concepto) {
    }

    public record ResumenCartera(BigDecimal capitalPendiente, BigDecimal deudaVencida, BigDecimal interesesPorCobrar,
                                 BigDecimal interesesCobrados, BigDecimal moraCobrada, BigDecimal totalCobrado,
                                 List<PuntoSerie> porModalidad, List<PuntoSerie> porEstado) {
    }

    /**
     * Panel principal del vendedor.
     */
    public record PanelVendedor(LocalDate fecha, int clientesActivos, int clientesConDeuda, int clientesEnMora,
                                BigDecimal carteraPendiente, BigDecimal deudaVencida, BigDecimal interesesPorCobrar,
                                BigDecimal totalCobrado, BigDecimal ventasDelMes, List<PuntoSerie> ventasPorMes,
                                List<PuntoSerie> cobrosPorMes, List<PuntoSerie> ventasPorModalidad,
                                List<FilaProductoVendido> topProductos, List<FilaDeudor> topDeudores,
                                List<FilaMoroso> morosos, List<ProximoVencimiento> proximosVencimientos) {
    }
}
