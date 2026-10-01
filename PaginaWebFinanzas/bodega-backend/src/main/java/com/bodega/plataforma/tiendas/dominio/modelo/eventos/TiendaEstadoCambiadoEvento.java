package com.bodega.plataforma.tiendas.dominio.modelo.eventos;

/**
 * Evento de dominio: una tienda fue creada, actualizada, dada de baja o reactivada.
 */
public record TiendaEstadoCambiadoEvento(Long tiendaId, String accion, String detalle) {
}
