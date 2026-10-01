package com.bodega.plataforma.estadocuenta.dominio.modelo.entidades;

import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoLinea;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Línea de un estado de cuenta. Las líneas informativas no suman al total a pagar.
 */
@Getter
public class LineaEstadoCuenta {

    @Setter
    private Long id;
    private final TipoLinea tipo;
    private final LocalDate fecha;
    private final String descripcion;
    private final BigDecimal monto;
    private final boolean incluidaEnTotal;

    public LineaEstadoCuenta(TipoLinea tipo, LocalDate fecha, String descripcion, BigDecimal monto,
                             boolean incluidaEnTotal) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.monto = monto;
        this.incluidaEnTotal = incluidaEnTotal;
    }
}
