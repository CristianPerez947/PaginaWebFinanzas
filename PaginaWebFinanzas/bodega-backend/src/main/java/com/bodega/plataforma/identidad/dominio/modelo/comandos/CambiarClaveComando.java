package com.bodega.plataforma.identidad.dominio.modelo.comandos;

public record CambiarClaveComando(Long usuarioId, String nuevaClave) {
    public CambiarClaveComando {
        if (usuarioId == null) throw new IllegalArgumentException("El usuario es obligatorio");
        if (nuevaClave == null || nuevaClave.length() < 6)
            throw new IllegalArgumentException("La clave debe tener al menos 6 caracteres");
    }
}
