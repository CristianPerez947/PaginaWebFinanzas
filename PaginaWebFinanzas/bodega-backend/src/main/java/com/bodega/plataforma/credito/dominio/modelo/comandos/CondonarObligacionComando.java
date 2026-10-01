package com.bodega.plataforma.credito.dominio.modelo.comandos;

/**
 * Condona una obligación pendiente (no se borra: queda registrada con su motivo).
 */
public record CondonarObligacionComando(Long tiendaId, Long obligacionId, String motivo) {
    public CondonarObligacionComando {
        if (tiendaId == null || obligacionId == null) throw new IllegalArgumentException("La obligación es obligatoria");
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("El motivo es obligatorio");
    }
}
