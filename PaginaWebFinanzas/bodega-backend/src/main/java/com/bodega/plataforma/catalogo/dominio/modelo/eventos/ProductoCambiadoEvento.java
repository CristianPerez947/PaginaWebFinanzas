package com.bodega.plataforma.catalogo.dominio.modelo.eventos;

/**
 * Evento de dominio: un producto fue creado, actualizado, dado de baja o reactivado.
 */
public record ProductoCambiadoEvento(Long tiendaId, Long productoId, String accion, String detalle) {
}
