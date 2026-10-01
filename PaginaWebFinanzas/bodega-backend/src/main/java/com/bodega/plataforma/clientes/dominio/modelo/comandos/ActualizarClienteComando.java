package com.bodega.plataforma.clientes.dominio.modelo.comandos;

import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.CondicionesCredito;

/**
 * Comando para actualizar los datos y condiciones de un cliente. El DNI no se modifica.
 */
public record ActualizarClienteComando(Long tiendaId, Long clienteId, String nombres, String apellidos,
                                       String telefono, String direccion, CondicionesCredito condiciones) {
    public ActualizarClienteComando {
        if (tiendaId == null || clienteId == null) throw new IllegalArgumentException("El cliente es obligatorio");
        CrearClienteComando.validarDatosPersonales(nombres, apellidos, telefono);
        if (condiciones == null) throw new IllegalArgumentException("Las condiciones de crédito son obligatorias");
    }
}
