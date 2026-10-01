package com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor;

/**
 * Tipo de línea de un estado de cuenta.
 */
public enum TipoLinea {
    /** Compra a fin de mes (capital). */
    COMPRA,
    /** Interés compensatorio por los días entre la compra y la fecha de pago. */
    INTERES_COMPENSATORIO,
    /** Cuota mensual de un plan de pagos. */
    CUOTA,
    /** Intereses por mora, en un ítem aparte. */
    MORA,
    /** Compra en cuotas realizada en el ciclo; solo informativa (se cobra en sus cuotas). */
    COMPRA_EN_CUOTAS
}
