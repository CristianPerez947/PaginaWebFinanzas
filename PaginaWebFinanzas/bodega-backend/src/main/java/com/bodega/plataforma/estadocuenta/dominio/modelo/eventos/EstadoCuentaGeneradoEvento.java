package com.bodega.plataforma.estadocuenta.dominio.modelo.eventos;

/**
 * Evento de dominio: se generó (o regeneró) un estado de cuenta.
 */
public record EstadoCuentaGeneradoEvento(Long tiendaId, Long clienteId, Long estadoCuentaId, String detalle) {
}
