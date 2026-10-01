package com.bodega.plataforma.clientes.interfaces.rest.recursos;

import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.Capitalizacion;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.Moneda;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.TipoTasa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "GuardarClienteSolicitud", description = "Alta o edición de un cliente y sus condiciones de crédito")
public record GuardarClienteRecurso(
        @Schema(description = "DNI de 8 dígitos (solo al crear)", example = "45678912") String dni,
        @Schema(example = "María") String nombres,
        @Schema(example = "Quispe Rojas") String apellidos,
        @Schema(description = "Teléfono móvil de 9 dígitos", example = "987654321") String telefono,
        @Schema(example = "Jr. Las Flores 456") String direccion,
        @Schema(description = "Moneda del crédito", example = "PEN") Moneda moneda,
        @Schema(description = "Máximo de deuda pendiente", example = "500.00") BigDecimal limiteCredito,
        @Schema(description = "Máximo de meses en cuotas", example = "3") int maxMeses,
        @Schema(description = "Día de corte (1 a 28)", example = "20") int diaCorte,
        @Schema(description = "Hora límite del corte (HH:mm)", example = "23:59") String horaCorte,
        @Schema(description = "Día de pago (1 a 28)", example = "26") int diaPago,
        @Schema(description = "Tipo de la tasa compensatoria", example = "EFECTIVA") TipoTasa tipoTasa,
        @Schema(description = "Tasa compensatoria anual en %", example = "24.0000000") BigDecimal tasaCompensatoria,
        @Schema(description = "Capitalización (solo tasa nominal)", example = "MENSUAL") Capitalizacion capitalizacion,
        @Schema(description = "Tipo de la tasa moratoria", example = "EFECTIVA") TipoTasa tipoTasaMora,
        @Schema(description = "Tasa moratoria anual en %", example = "36.0000000") BigDecimal tasaMora,
        @Schema(description = "Capitalización de la moratoria (solo nominal)") Capitalizacion capitalizacionMora) {
}
