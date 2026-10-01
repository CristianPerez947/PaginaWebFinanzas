package com.bodega.plataforma.tiendas.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "CrearTiendaSolicitud", description = "Alta de una tienda con su vendedor")
public record CrearTiendaRecurso(
        @Schema(example = "La Tiendita de Don Pepe") String nombre,
        @Schema(description = "Identificador en la URL", example = "tiendita-don-pepe") String slug,
        @Schema(description = "Archivo del logo en assets", example = "logo-don-pepe.png") String logo,
        @Schema(description = "RUC de 11 dígitos", example = "20123456789") String ruc,
        @Schema(example = "Av. Los Olivos 123, Lima") String direccion,
        @Schema(example = "987654321") String telefono,
        @Schema(description = "Soles por dólar", example = "3.75") BigDecimal tipoCambioUsd,
        @Schema(example = "José Pérez") String nombreDueno,
        @Schema(description = "Usuario de acceso del vendedor", example = "pepe") String usuarioVendedor,
        @Schema(description = "Clave inicial (mínimo 6 caracteres)", example = "pepe123") String claveVendedor) {
}
