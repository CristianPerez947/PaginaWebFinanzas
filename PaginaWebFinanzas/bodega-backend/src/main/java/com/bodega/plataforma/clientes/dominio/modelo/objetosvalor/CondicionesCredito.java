package com.bodega.plataforma.clientes.dominio.modelo.objetosvalor;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Condiciones de crédito pactadas con un cliente.
 *
 * @param moneda        moneda del crédito
 * @param limiteCredito máximo de deuda pendiente permitido
 * @param maxMeses      máximo número de meses (cuotas) que se le puede dar
 * @param diaCorte      día del mes del corte (1 a 28)
 * @param horaCorte     hora límite del día de corte; las compras posteriores pasan al ciclo siguiente
 * @param diaPago       día del mes en que se compromete a pagar (1 a 28)
 * @param tasaCompensatoria interés compensatorio pactado
 * @param tasaMoratoria     interés moratorio pactado
 */
public record CondicionesCredito(Moneda moneda, BigDecimal limiteCredito, int maxMeses, int diaCorte,
                                 LocalTime horaCorte, int diaPago, TasaInteres tasaCompensatoria,
                                 TasaInteres tasaMoratoria) {

    public CondicionesCredito {
        if (moneda == null) throw new IllegalArgumentException("La moneda es obligatoria");
        if (limiteCredito == null || limiteCredito.signum() <= 0)
            throw new IllegalArgumentException("El límite de crédito debe ser mayor que cero");
        if (maxMeses < 1 || maxMeses > 24)
            throw new IllegalArgumentException("El máximo de meses debe estar entre 1 y 24");
        if (diaCorte < 1 || diaCorte > 28)
            throw new IllegalArgumentException("El día de corte debe estar entre 1 y 28");
        if (diaPago < 1 || diaPago > 28)
            throw new IllegalArgumentException("El día de pago debe estar entre 1 y 28");
        if (horaCorte == null) horaCorte = LocalTime.of(23, 59);
        if (tasaCompensatoria == null) throw new IllegalArgumentException("La tasa compensatoria es obligatoria");
        if (tasaMoratoria == null) throw new IllegalArgumentException("La tasa moratoria es obligatoria");
    }
}
