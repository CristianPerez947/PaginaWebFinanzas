package com.bodega.plataforma.catalogo.dominio.modelo.comandos;

import java.math.BigDecimal;

public record ActualizarProductoComando(Long tiendaId, Long productoId, String nombre, String categoria, String marca,
                                        String proveedor, String descripcion, String unidadMedida, String imagen,
                                        BigDecimal precioContado, BigDecimal precioCredito, boolean permiteCuotas) {
    public ActualizarProductoComando {
        if (tiendaId == null || productoId == null) throw new IllegalArgumentException("El producto es obligatorio");
        CrearProductoComando.validarDatos(nombre, unidadMedida, precioContado, precioCredito);
        if (precioCredito == null) precioCredito = precioContado;
    }
}
