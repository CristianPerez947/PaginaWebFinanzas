package com.bodega.plataforma.credito.dominio.modelo.agregados;

import com.bodega.plataforma.compartido.dominio.modelo.agregados.RaizAgregadoDominio;
import com.bodega.plataforma.credito.dominio.modelo.entidades.LineaVenta;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Raíz de agregado VentaCredito: venta registrada por el vendedor a un cliente, con sus líneas.
 * Los importes están en la moneda del crédito del cliente. Una venta no se anula (regla del negocio).
 */
@Getter
public class VentaCredito extends RaizAgregadoDominio<VentaCredito> {

    @Setter
    private Long id;
    @Setter
    private Long tiendaId;
    @Setter
    private Long clienteId;
    @Setter
    private LocalDateTime fechaHora;
    @Setter
    private String moneda;
    /** Tipo de cambio congelado en la venta; nulo si la moneda es soles. */
    @Setter
    private BigDecimal tipoCambioAplicado;
    @Setter
    private BigDecimal total;
    @Setter
    private List<LineaVenta> lineas = new ArrayList<>();

    public VentaCredito() {
    }

    public VentaCredito(Long tiendaId, Long clienteId, LocalDateTime fechaHora, String moneda,
                        BigDecimal tipoCambioAplicado, List<LineaVenta> lineas) {
        this.tiendaId = tiendaId;
        this.clienteId = clienteId;
        this.fechaHora = fechaHora;
        this.moneda = moneda;
        this.tipoCambioAplicado = tipoCambioAplicado;
        this.lineas = new ArrayList<>(lineas);
        this.total = lineas.stream().map(LineaVenta::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
