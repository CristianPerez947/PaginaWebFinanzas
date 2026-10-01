package com.bodega.plataforma.catalogo.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "GuardarProductoSolicitud", description = "Alta o edición de un producto")
public record GuardarProductoRecurso(
        @Schema(example = "Arroz Costeño 1 kg") String nombre,
        @Schema(example = "Abarrotes") String categoria,
        @Schema(example = "Costeño") String marca,
        @Schema(example = "Alicorp") String proveedor,
        @Schema(example = "Arroz extra añejo") String descripcion,
        @Schema(example = "kg") String unidadMedida,
        @Schema(description = "Archivo en assets/productos", example = "arroz-costeno.jpg") String imagen,
        @Schema(example = "4.20") BigDecimal precioContado,
        @Schema(description = "Si se omite, es igual al contado", example = "4.20") BigDecimal precioCredito,
        @Schema(description = "Permite ventas en cuotas mensuales", example = "false") boolean permiteCuotas) {
}
