package com.bodega.plataforma.clientes.dominio.modelo.eventos;

/**
 * Evento de dominio: un cliente fue creado, actualizado, dado de baja, reactivado o se regeneró su clave.
 */
public record ClienteCambiadoEvento(Long tiendaId, Long clienteId, String accion, String detalle) {
}
