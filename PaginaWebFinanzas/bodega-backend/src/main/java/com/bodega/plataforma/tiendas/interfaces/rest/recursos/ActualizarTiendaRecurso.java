package com.bodega.plataforma.tiendas.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "ActualizarTiendaSolicitud")
public record ActualizarTiendaRecurso(String nombre, String logo, String ruc, String direccion, String telefono,
                                      BigDecimal tipoCambioUsd, String nombreDueno) {
}
