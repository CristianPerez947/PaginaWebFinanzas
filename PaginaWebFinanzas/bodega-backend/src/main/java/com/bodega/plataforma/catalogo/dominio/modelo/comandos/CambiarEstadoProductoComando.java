package com.bodega.plataforma.catalogo.dominio.modelo.comandos;

/**
 * Da de baja (activo = false) o reactiva un producto. Baja lógica: el historial de ventas se conserva.
 */
public record CambiarEstadoProductoComando(Long tiendaId, Long productoId, boolean activo) {
    public CambiarEstadoProductoComando {
        if (tiendaId == null || productoId == null) throw new IllegalArgumentException("El producto es obligatorio");
    }
}
