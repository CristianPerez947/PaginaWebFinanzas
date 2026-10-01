package com.bodega.plataforma.clientes.dominio.modelo.comandos;

/**
 * Da de baja (activo = false) o reactiva un cliente. Un cliente de baja no puede ingresar ni recibir crédito.
 */
public record CambiarEstadoClienteComando(Long tiendaId, Long clienteId, boolean activo) {
    public CambiarEstadoClienteComando {
        if (tiendaId == null || clienteId == null) throw new IllegalArgumentException("El cliente es obligatorio");
    }
}
