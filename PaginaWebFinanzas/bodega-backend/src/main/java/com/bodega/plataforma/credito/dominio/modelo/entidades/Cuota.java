package com.bodega.plataforma.credito.dominio.modelo.entidades;

import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.EstadoDeuda;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cuota de un plan de pagos francés. Se paga completa (no hay pagos parciales).
 */
@Getter
public class Cuota {

    @Setter
    private Long id;
    private final int numero;
    private final LocalDate fechaVencimiento;
    private final BigDecimal cuota;
    private final BigDecimal interes;
    private final BigDecimal amortizacion;
    private final BigDecimal saldo;
    @Setter
    private EstadoDeuda estado;
    @Setter
    private LocalDate fechaPagoReal;

    public Cuota(int numero, LocalDate fechaVencimiento, BigDecimal cuota, BigDecimal interes, BigDecimal amortizacion,
                 BigDecimal saldo, EstadoDeuda estado) {
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
        this.cuota = cuota;
        this.interes = interes;
        this.amortizacion = amortizacion;
        this.saldo = saldo;
        this.estado = estado;
    }

    public boolean estaPendiente() {
        return estado == EstadoDeuda.PENDIENTE;
    }
}
