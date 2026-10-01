package com.bodega.plataforma.identidad.dominio.modelo.comandos;

/**
 * Activa o desactiva a todos los usuarios de una tienda (baja o reactivación de la tienda).
 */
public record CambiarEstadoUsuariosDeTiendaComando(Long tiendaId, boolean activo) {
    public CambiarEstadoUsuariosDeTiendaComando {
        if (tiendaId == null) throw new IllegalArgumentException("La tienda es obligatoria");
    }
}
