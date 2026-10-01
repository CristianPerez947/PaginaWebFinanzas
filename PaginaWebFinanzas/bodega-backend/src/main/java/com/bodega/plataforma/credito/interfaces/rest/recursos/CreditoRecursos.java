package com.bodega.plataforma.credito.interfaces.rest.recursos;

import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.EstadoDeuda;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Recursos (DTO) del contexto de crédito.
 */
public final class CreditoRecursos {

    private CreditoRecursos() {
    }

    @Schema(name = "CuotaRespuesta", description = "Cuota del plan de pagos con su mora a la fecha de consulta")
    public record CuotaRecurso(int numero, LocalDate fechaVencimiento, BigDecimal cuota, BigDecimal interes,
                               BigDecimal amortizacion, BigDecimal saldo, EstadoDeuda estado, LocalDate fechaPagoReal,
                               long diasMora, BigDecimal mora, BigDecimal totalAPagar) {
    }

    @Schema(name = "PagoRespuesta", description = "Pago registrado, con la prelación mora, interés y capital")
    public record PagoRecurso(Integer numeroCuota, LocalDate fechaPago, BigDecimal montoMora, BigDecimal montoInteres,
                              BigDecimal montoCapital, BigDecimal total) {
    }

    @Schema(name = "ObligacionRespuesta", description = "Deuda originada por una venta a crédito")
    public record ObligacionRecurso(
            Long id, Long ventaId, Modalidad modalidad, String moneda, BigDecimal capital,
            BigDecimal interesCompensatorio, LocalDateTime fechaCompra, LocalDate fechaCorte, LocalDate fechaPago,
            long diasGracia, int numCuotas, BigDecimal teaCompensatoria, BigDecimal teaMora, EstadoDeuda estado,
            boolean enMora, long diasMora, BigDecimal mora, BigDecimal importeExigible,
            List<CuotaRecurso> cuotas, List<PagoRecurso> pagos, String motivoCondonacion, LocalDate fechaCondonacion) {
    }

    @Schema(name = "LineaVentaRespuesta")
    public record LineaVentaRecurso(Long productoId, String nombreProducto, BigDecimal cantidad,
                                    BigDecimal precioUnitario, BigDecimal subtotal, Modalidad modalidad,
                                    Integer numCuotas) {
    }

    @Schema(name = "VentaRespuesta", description = "Venta con sus líneas y, si corresponde, las obligaciones generadas")
    public record VentaRecurso(Long id, Long clienteId, LocalDateTime fechaHora, String moneda,
                               BigDecimal tipoCambioAplicado, BigDecimal total, List<LineaVentaRecurso> lineas,
                               List<ObligacionRecurso> obligaciones) {
    }

    @Schema(name = "LineaVentaSolicitud")
    public record LineaVentaSolicitud(
            @Schema(description = "Producto activo de la tienda", example = "1") Long productoId,
            @Schema(description = "Cantidad (admite decimales)", example = "2") BigDecimal cantidad,
            @Schema(description = "CONTADO, FIN_DE_MES o CUOTAS", example = "FIN_DE_MES") Modalidad modalidad,
            @Schema(description = "Número de cuotas (solo CUOTAS)", example = "3") Integer numCuotas) {
    }

    @Schema(name = "RegistrarVentaSolicitud")
    public record RegistrarVentaSolicitud(
            @Schema(description = "Cliente que recibe el crédito", example = "1") Long clienteId,
            List<LineaVentaSolicitud> lineas) {
    }

    @Schema(name = "DeudaClienteRespuesta", description = "Situación de deuda de un cliente a la fecha del sistema")
    public record DeudaClienteRecurso(Long clienteId, String moneda, BigDecimal limiteCredito,
                                      BigDecimal capitalPendiente, BigDecimal disponible, boolean enMora,
                                      BigDecimal totalVencido, LocalDate fechaConsulta,
                                      List<ObligacionRecurso> obligaciones) {
    }

    @Schema(name = "PagoCicloSolicitud", description = "Pago total de las obligaciones a fin de mes de un ciclo")
    public record PagoCicloSolicitud(
            @Schema(description = "Fecha de pago pactada del ciclo", example = "2026-09-26") LocalDate fechaPagoCiclo,
            @Schema(description = "Fecha real del pago; si se omite, la fecha actual del sistema", example = "2026-09-26")
            LocalDate fechaPago) {
    }

    @Schema(name = "PagoCuotaSolicitud")
    public record PagoCuotaSolicitud(
            @Schema(description = "Fecha real del pago; si se omite, la fecha actual del sistema") LocalDate fechaPago) {
    }

    @Schema(name = "CondonacionSolicitud")
    public record CondonacionSolicitud(
            @Schema(description = "Motivo de la condonación (obligatorio)", example = "Fallecimiento del titular")
            String motivo) {
    }
}
