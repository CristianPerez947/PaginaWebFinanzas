package com.bodega.plataforma.clientes.dominio.modelo.objetosvalor;

import java.math.BigDecimal;

/**
 * Tasa de interés pactada, expresada en porcentaje anual (por ejemplo 24.0000000 = 24 %).
 * Si es nominal, indica además su capitalización; si es efectiva, la capitalización no se usa.
 *
 * @param tipo           efectiva o nominal
 * @param porcentaje     tasa anual en porcentaje, mayor que cero, con hasta 7 decimales
 * @param capitalizacion capitalización de la tasa nominal (obligatoria si es nominal)
 */
public record TasaInteres(TipoTasa tipo, BigDecimal porcentaje, Capitalizacion capitalizacion) {

    public TasaInteres {
        if (tipo == null) throw new IllegalArgumentException("El tipo de tasa es obligatorio");
        if (porcentaje == null || porcentaje.signum() <= 0)
            throw new IllegalArgumentException("La tasa debe ser mayor que cero");
        if (porcentaje.compareTo(new BigDecimal("1000")) > 0)
            throw new IllegalArgumentException("La tasa no puede superar el 1000 %");
        if (tipo == TipoTasa.NOMINAL && capitalizacion == null)
            throw new IllegalArgumentException("Una tasa nominal requiere periodo de capitalización");
        if (tipo == TipoTasa.EFECTIVA) capitalizacion = null;
    }
}
