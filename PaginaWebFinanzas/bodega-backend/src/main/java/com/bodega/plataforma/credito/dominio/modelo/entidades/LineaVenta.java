package com.bodega.plataforma.credito.dominio.modelo.entidades;

import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Línea de una venta: producto, cantidad, precio congelado y forma de pago.
 */
@Getter
public class LineaVenta {

    @Setter
    private Long id;
    private final Long productoId;
    private final String nombreProducto;
    private final BigDecimal cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;
    private final Modalidad modalidad;
    /** Número de cuotas si la modalidad es CUOTAS; nulo en otro caso. */
    private final Integer numCuotas;

    public LineaVenta(Long productoId, String nombreProducto, BigDecimal cantidad, BigDecimal precioUnitario,
                      BigDecimal subtotal, Modalidad modalidad, Integer numCuotas) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.modalidad = modalidad;
        this.numCuotas = numCuotas;
    }
}
