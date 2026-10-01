package com.bodega.plataforma.identidad.dominio.modelo.comandos;

public record CambiarEstadoUsuarioComando(Long usuarioId, boolean activo) {
    public CambiarEstadoUsuarioComando {
        if (usuarioId == null) throw new IllegalArgumentException("El usuario es obligatorio");
    }
}
