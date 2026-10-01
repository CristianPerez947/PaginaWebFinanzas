package com.bodega.plataforma.identidad.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IniciarSesionClienteSolicitud", description = "Acceso de un cliente a la cuenta de una tienda")
public record IniciarSesionClienteRecurso(
        @Schema(description = "Identificador de la tienda", example = "1") Long tiendaId,
        @Schema(description = "DNI del cliente (8 dígitos)", example = "45678912") String dni,
        @Schema(description = "Clave asignada por el vendedor", example = "482913") String clave) {
}
