package com.bodega.plataforma.tiendas.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "TiendaRespuesta", description = "Datos completos de una tienda (administrador del sistema)")
public record TiendaRecurso(
        Long id, String nombre, String slug, String logo, String ruc, String direccion, String telefono,
        BigDecimal tipoCambioUsd, boolean activa, String nombreDueno, String usuarioVendedor) {
}
