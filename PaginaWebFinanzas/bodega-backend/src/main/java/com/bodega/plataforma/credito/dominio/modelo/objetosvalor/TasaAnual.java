package com.bodega.plataforma.credito.dominio.modelo.objetosvalor;

import java.math.BigDecimal;

/**
 * Tasa pactada tal como llega desde el contexto de clientes.
 *
 * @param nominal            verdadero si es nominal anual; falso si es efectiva anual
 * @param porcentaje         tasa anual en porcentaje (24 = 24 %)
 * @param capitalizacionDias días de capitalización de una tasa nominal (1 = diaria, 30 = mensual); ignorado si es efectiva
 */
public record TasaAnual(boolean nominal, BigDecimal porcentaje, int capitalizacionDias) {
}
