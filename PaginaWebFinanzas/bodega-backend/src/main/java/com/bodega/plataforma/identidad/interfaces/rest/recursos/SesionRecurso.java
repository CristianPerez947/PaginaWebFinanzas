package com.bodega.plataforma.identidad.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SesionRespuesta", description = "Sesión iniciada con su token de acceso")
public record SesionRecurso(Long id, String usuario, String rol, Long tiendaId, String token) {
}
