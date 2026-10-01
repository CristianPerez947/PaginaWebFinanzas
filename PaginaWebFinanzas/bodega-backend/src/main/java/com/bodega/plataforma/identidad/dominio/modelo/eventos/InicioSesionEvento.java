package com.bodega.plataforma.identidad.dominio.modelo.eventos;

/**
 * Evento de dominio: un usuario inició sesión correctamente.
 */
public record InicioSesionEvento(Long usuarioId, String nombreUsuario, String rol, Long tiendaId) {
}
