package com.bodega.plataforma.credito.dominio.modelo.entidades;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pago registrado por el vendedor. El desglose respeta la prelación: mora, interés compensatorio y capital.
 */
@Getter
public class Pago {

    @Setter
    private Long id;
    /** Número de cuota pagada; nulo si el pago cancela una obligación a fin de mes. */
    private final Integer numeroCuota;
    private final LocalDate fechaPago;
    private final BigDecimal montoMora;
    private final BigDecimal montoInteres;
    private final BigDecimal montoCapital;

    public Pago(Integer numeroCuota, LocalDate fechaPago, BigDecimal montoMora, BigDecimal montoInteres,
                BigDecimal montoCapital) {
        this.numeroCuota = numeroCuota;
        this.fechaPago = fechaPago;
        this.montoMora = montoMora;
        this.montoInteres = montoInteres;
        this.montoCapital = montoCapital;
    }

    public BigDecimal total() {
        return montoMora.add(montoInteres).add(montoCapital);
    }
}
