package com.bodega.plataforma.credito.dominio.modelo.comandos;

/**
 * Condona toda la deuda pendiente de un cliente (con motivo). El historial se conserva.
 */
public record CondonarDeudaClienteComando(Long tiendaId, Long clienteId, String motivo) {
    public CondonarDeudaClienteComando {
        if (tiendaId == null || clienteId == null) throw new IllegalArgumentException("El cliente es obligatorio");
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("El motivo es obligatorio");
    }
}
