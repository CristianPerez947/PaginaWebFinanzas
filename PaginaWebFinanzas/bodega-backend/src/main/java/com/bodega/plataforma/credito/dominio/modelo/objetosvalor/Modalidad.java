package com.bodega.plataforma.credito.dominio.modelo.objetosvalor;

/**
 * Forma de pago de una línea de venta.
 */
public enum Modalidad {
    /** Pago inmediato: se registra la venta pero no genera deuda. */
    CONTADO,
    /** Crédito a pagar en la fecha de pago del ciclo, con intereses por los días transcurridos. */
    FIN_DE_MES,
    /** Crédito en cuotas mensuales (método francés vencido, ordinario, meses de 30 días). */
    CUOTAS
}
