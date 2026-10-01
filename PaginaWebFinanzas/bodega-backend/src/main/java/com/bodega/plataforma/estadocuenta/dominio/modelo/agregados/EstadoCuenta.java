package com.bodega.plataforma.estadocuenta.dominio.modelo.agregados;

import com.bodega.plataforma.compartido.dominio.modelo.agregados.RaizAgregadoDominio;
import com.bodega.plataforma.compartido.dominio.servicios.Redondeo;
import com.bodega.plataforma.estadocuenta.dominio.modelo.entidades.LineaEstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.EstadoEstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoLinea;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Raíz de agregado EstadoCuenta: listado de pago de un cliente para un ciclo (fecha de corte y de pago),
 * con las compras ordenadas por fecha, sus intereses y, en un ítem aparte, los intereses por mora.
 */
@Getter
public class EstadoCuenta extends RaizAgregadoDominio<EstadoCuenta> {

    @Setter
    private Long id;
    @Setter
    private Long tiendaId;
    @Setter
    private Long clienteId;
    @Setter
    private String moneda;
    @Setter
    private LocalDate fechaCorte;
    @Setter
    private LocalDate fechaPago;
    @Setter
    private LocalDateTime fechaGeneracion;
    @Setter
    private BigDecimal totalCompras = Redondeo.dinero(BigDecimal.ZERO);
    @Setter
    private BigDecimal totalIntereses = Redondeo.dinero(BigDecimal.ZERO);
    @Setter
    private BigDecimal totalCuotas = Redondeo.dinero(BigDecimal.ZERO);
    @Setter
    private BigDecimal totalMora = Redondeo.dinero(BigDecimal.ZERO);
    @Setter
    private BigDecimal totalAPagar = Redondeo.dinero(BigDecimal.ZERO);
    @Setter
    private EstadoEstadoCuenta estado = EstadoEstadoCuenta.PENDIENTE;
    @Setter
    private List<LineaEstadoCuenta> lineas = new ArrayList<>();

    public EstadoCuenta() {
    }

    public EstadoCuenta(Long tiendaId, Long clienteId, String moneda, LocalDate fechaCorte, LocalDate fechaPago) {
        this.tiendaId = tiendaId;
        this.clienteId = clienteId;
        this.moneda = moneda;
        this.fechaCorte = fechaCorte;
        this.fechaPago = fechaPago;
    }

    /** Reemplaza las líneas por las recién calculadas y recalcula los totales por tipo. */
    public void reemplazarLineas(List<LineaEstadoCuenta> nuevas, LocalDateTime fechaGeneracion) {
        this.lineas = new ArrayList<>(nuevas);
        this.fechaGeneracion = fechaGeneracion;
        this.totalCompras = sumar(TipoLinea.COMPRA);
        this.totalIntereses = sumar(TipoLinea.INTERES_COMPENSATORIO);
        this.totalCuotas = sumar(TipoLinea.CUOTA);
        this.totalMora = sumar(TipoLinea.MORA);
        this.totalAPagar = Redondeo.dinero(totalCompras.add(totalIntereses).add(totalCuotas).add(totalMora));
        this.estado = EstadoEstadoCuenta.PENDIENTE;
    }

    /** Marca el estado de cuenta como pagado; conserva las últimas líneas calculadas. */
    public void marcarPagado() {
        this.estado = EstadoEstadoCuenta.PAGADO;
    }

    public boolean estaPendiente() {
        return estado == EstadoEstadoCuenta.PENDIENTE;
    }

    private BigDecimal sumar(TipoLinea tipo) {
        return Redondeo.dinero(lineas.stream().filter(l -> l.getTipo() == tipo && l.isIncluidaEnTotal())
                .map(LineaEstadoCuenta::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
