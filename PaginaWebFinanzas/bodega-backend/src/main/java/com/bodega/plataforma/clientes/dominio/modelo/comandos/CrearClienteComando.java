package com.bodega.plataforma.clientes.dominio.modelo.comandos;

import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.CondicionesCredito;

/**
 * Comando para dar de alta un cliente con sus condiciones de crédito.
 *
 * @param tiendaId    tienda que le otorga el crédito (se toma del token)
 * @param dni         DNI de 8 dígitos; es único por tienda
 * @param nombres     nombres del cliente
 * @param apellidos   apellidos del cliente
 * @param telefono    teléfono móvil de 9 dígitos (se usa para enviar la clave y los avisos)
 * @param direccion   dirección; opcional
 * @param condiciones condiciones de crédito pactadas
 */
public record CrearClienteComando(Long tiendaId, String dni, String nombres, String apellidos, String telefono,
                                  String direccion, CondicionesCredito condiciones) {
    public CrearClienteComando {
        if (tiendaId == null) throw new IllegalArgumentException("La tienda es obligatoria");
        if (dni == null || !dni.matches("\\d{8}")) throw new IllegalArgumentException("El DNI debe tener 8 dígitos");
        validarDatosPersonales(nombres, apellidos, telefono);
        if (condiciones == null) throw new IllegalArgumentException("Las condiciones de crédito son obligatorias");
    }

    static void validarDatosPersonales(String nombres, String apellidos, String telefono) {
        if (nombres == null || nombres.isBlank()) throw new IllegalArgumentException("Los nombres son obligatorios");
        if (apellidos == null || apellidos.isBlank()) throw new IllegalArgumentException("Los apellidos son obligatorios");
        if (telefono == null || !telefono.matches("\\d{9}"))
            throw new IllegalArgumentException("El teléfono debe tener 9 dígitos");
    }
}
