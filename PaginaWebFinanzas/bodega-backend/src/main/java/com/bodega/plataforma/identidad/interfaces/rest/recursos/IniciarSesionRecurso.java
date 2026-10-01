package com.bodega.plataforma.identidad.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IniciarSesionSolicitud", description = "Acceso del vendedor o del administrador del sistema")
public record IniciarSesionRecurso(
        @Schema(description = "Usuario", example = "pepe") String usuario,
        @Schema(description = "Clave", example = "pepe123") String clave) {
}
