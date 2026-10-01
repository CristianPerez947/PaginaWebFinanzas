package com.bodega.plataforma.catalogo.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "ProductoRespuesta", description = "Producto o servicio de la tienda")
public record ProductoRecurso(
        Long id, String nombre, String categoria, String marca, String proveedor, String descripcion,
        String unidadMedida, String imagen, BigDecimal precioContado, BigDecimal precioCredito,
        boolean permiteCuotas, boolean activo) {
}
