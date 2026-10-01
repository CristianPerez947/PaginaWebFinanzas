package com.bodega.plataforma.estadocuenta.interfaces.rest.recursos;

import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.EstadoEstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoLinea;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoMensaje;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Recursos (DTO) del contexto de estados de cuenta.
 */
public final class EstadoCuentaRecursos {

    private EstadoCuentaRecursos() {
    }

    @Schema(name = "LineaEstadoCuentaRespuesta")
    public record LineaEstadoCuentaRecurso(TipoLinea tipo, LocalDate fecha, String descripcion, BigDecimal monto,
                                           boolean incluidaEnTotal) {
    }

    @Schema(name = "EstadoCuentaRespuesta", description = "Listado de pago de un cliente para un ciclo")
    public record EstadoCuentaRecurso(Long id, Long clienteId, String nombreCliente, String dniCliente, String moneda,
                                      LocalDate fechaCorte, LocalDate fechaPago, LocalDateTime fechaGeneracion,
                                      BigDecimal totalCompras, BigDecimal totalIntereses, BigDecimal totalCuotas,
                                      BigDecimal totalMora, BigDecimal totalAPagar, EstadoEstadoCuenta estado,
                                      List<LineaEstadoCuentaRecurso> lineas) {
    }

    @Schema(name = "GenerarEstadoCuentaSolicitud")
    public record GenerarEstadoCuentaSolicitud(
            @Schema(description = "Cliente", example = "1") Long clienteId,
            @Schema(description = "Fecha de corte del ciclo; si se omite, el último corte vencido", example = "2026-09-20")
            LocalDate fechaCorte) {
    }

    @Schema(name = "CortesGeneradosRespuesta")
    public record CortesGeneradosRecurso(int generados) {
    }

    @Schema(name = "MensajeWhatsAppRespuesta", description = "Aviso por WhatsApp (envío simulado)")
    public record MensajeWhatsAppRecurso(Long id, Long clienteId, String nombreCliente, String telefono,
                                         TipoMensaje tipo, String contenido, LocalDateTime fechaEnvio,
                                         String estadoEnvio) {
    }
}
