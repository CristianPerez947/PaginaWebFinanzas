package com.bodega.plataforma.tiendas.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "TiendaPublicaRespuesta", description = "Datos públicos de una tienda")
public record TiendaPublicaRecurso(
        Long id, String nombre, String slug, String logo, String direccion, String telefono,
        BigDecimal tipoCambioUsd) {
}
