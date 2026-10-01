package com.bodega.plataforma.clientes.dominio.modelo.comandos;

public record RegenerarClaveClienteComando(Long tiendaId, Long clienteId) {
    public RegenerarClaveClienteComando {
        if (tiendaId == null || clienteId == null) throw new IllegalArgumentException("El cliente es obligatorio");
    }
}
