package com.bodega.plataforma.tiendas.dominio.modelo.comandos;

/**
 * Da de baja (activa = false) o reactiva una tienda. La baja es lógica.
 */
public record CambiarEstadoTiendaComando(Long tiendaId, boolean activa) {
    public CambiarEstadoTiendaComando {
        if (tiendaId == null) throw new IllegalArgumentException("La tienda es obligatoria");
    }
}
