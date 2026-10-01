package com.bodega.plataforma.tiendas.dominio.modelo.comandos;

import java.math.BigDecimal;

public record ActualizarTiendaComando(Long tiendaId, String nombre, String logo, String ruc, String direccion,
                                      String telefono, BigDecimal tipoCambioUsd, String nombreDueno) {
    public ActualizarTiendaComando {
        if (tiendaId == null) throw new IllegalArgumentException("La tienda es obligatoria");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre de la tienda es obligatorio");
        if (ruc != null && !ruc.isBlank() && !ruc.matches("\\d{11}"))
            throw new IllegalArgumentException("El RUC debe tener 11 dígitos");
        if (nombreDueno == null || nombreDueno.isBlank()) throw new IllegalArgumentException("El nombre del dueño es obligatorio");
        if (tipoCambioUsd == null || tipoCambioUsd.signum() <= 0)
            throw new IllegalArgumentException("El tipo de cambio debe ser mayor que cero");
    }
}
