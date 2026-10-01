package com.bodega.plataforma.credito.dominio.modelo.agregados;

import com.bodega.plataforma.compartido.dominio.modelo.agregados.RaizAgregadoDominio;
import com.bodega.plataforma.compartido.dominio.servicios.Redondeo;
import com.bodega.plataforma.credito.dominio.modelo.entidades.Cuota;
import com.bodega.plataforma.credito.dominio.modelo.entidades.Pago;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Ciclo;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.EstadoDeuda;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import com.bodega.plataforma.credito.dominio.servicios.CalculadoraFinanciera;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Raíz de agregado Obligacion: deuda de un cliente originada en una venta a crédito.
 * <ul>
 *   <li>FIN_DE_MES: se paga completa en la fecha de pago del ciclo, con interés compensatorio por los días
 *       entre la compra y esa fecha; si se paga tarde se suma la mora.</li>
 *   <li>CUOTAS: plan francés; el periodo de gracia (compra → fecha de pago) se capitaliza antes de calcular la cuota.</li>
 * </ul>
 * No admite pagos parciales. La mora se calcula con la fecha que se consulte; nunca se almacena.
 */
@Getter
public class Obligacion extends RaizAgregadoDominio<Obligacion> {

    @Setter
    private Long id;
    @Setter
    private Long tiendaId;
    @Setter
    private Long clienteId;
    @Setter
    private Long ventaId;
    @Setter
    private Modalidad modalidad;
    @Setter
    private String moneda;
    @Setter
    private BigDecimal capital;
    /** FIN_DE_MES: interés por los días hasta la fecha de pago. CUOTAS: interés capitalizado en el periodo de gracia. */
    @Setter
    private BigDecimal interesCompensatorio;
    @Setter
    private LocalDateTime fechaCompra;
    @Setter
    private LocalDate fechaCorte;
    @Setter
    private LocalDate fechaPago;
    @Setter
    private long diasGracia;
    @Setter
    private int numCuotas;
    @Setter
    private BigDecimal teaCompensatoria;
    @Setter
    private BigDecimal teaMora;
    @Setter
    private EstadoDeuda estado = EstadoDeuda.PENDIENTE;
    @Setter
    private String motivoCondonacion;
    @Setter
    private LocalDate fechaCondonacion;
    @Setter
    private List<Cuota> cuotas = new ArrayList<>();
    @Setter
    private List<Pago> pagos = new ArrayList<>();

    public Obligacion() {
    }

    /** Crea una obligación a fin de mes. */
    public static Obligacion finDeMes(Long tiendaId, Long clienteId, String moneda, BigDecimal capital,
                                      LocalDateTime fechaCompra, Ciclo ciclo, BigDecimal teaCompensatoria,
                                      BigDecimal teaMora) {
        var o = base(tiendaId, clienteId, moneda, capital, fechaCompra, ciclo, teaCompensatoria, teaMora);
        o.modalidad = Modalidad.FIN_DE_MES;
        o.interesCompensatorio = CalculadoraFinanciera.interes(capital, teaCompensatoria, o.diasGracia);
        return o;
    }

    /** Crea una obligación en cuotas: capitaliza la gracia y genera el cronograma francés. */
    public static Obligacion enCuotas(Long tiendaId, Long clienteId, String moneda, BigDecimal capital,
                                      LocalDateTime fechaCompra, Ciclo ciclo, BigDecimal teaCompensatoria,
                                      BigDecimal teaMora, int numCuotas) {
        var o = base(tiendaId, clienteId, moneda, capital, fechaCompra, ciclo, teaCompensatoria, teaMora);
        o.modalidad = Modalidad.CUOTAS;
        o.numCuotas = numCuotas;
        var capitalizado = CalculadoraFinanciera.capitalizarGracia(capital, teaCompensatoria, o.diasGracia);
        o.interesCompensatorio = capitalizado.subtract(capital);
        o.cuotas = new ArrayList<>(CalculadoraFinanciera.cronogramaFrances(capitalizado, teaCompensatoria, numCuotas,
                ciclo.fechaPago()));
        return o;
    }

    private static Obligacion base(Long tiendaId, Long clienteId, String moneda, BigDecimal capital,
                                   LocalDateTime fechaCompra, Ciclo ciclo, BigDecimal teaCompensatoria,
                                   BigDecimal teaMora) {
        var o = new Obligacion();
        o.tiendaId = tiendaId;
        o.clienteId = clienteId;
        o.moneda = moneda;
        o.capital = Redondeo.dinero(capital);
        o.fechaCompra = fechaCompra;
        o.fechaCorte = ciclo.fechaCorte();
        o.fechaPago = ciclo.fechaPago();
        o.diasGracia = CalculadoraFinanciera.dias(fechaCompra.toLocalDate(), ciclo.fechaPago());
        o.teaCompensatoria = teaCompensatoria;
        o.teaMora = teaMora;
        o.estado = EstadoDeuda.PENDIENTE;
        return o;
    }

    // ---------- Consultas de estado (dependen de la fecha que se pase) ----------

    public boolean estaPendiente() {
        return estado == EstadoDeuda.PENDIENTE;
    }

    /** Monto sobre el que corre la mora en una obligación a fin de mes: capital + interés compensatorio. */
    public BigDecimal montoFinDeMes() {
        return capital.add(interesCompensatorio);
    }

    public long diasMoraFinDeMes(LocalDate al) {
        return estaPendiente() && modalidad == Modalidad.FIN_DE_MES ? CalculadoraFinanciera.dias(fechaPago, al) : 0;
    }

    public BigDecimal moraFinDeMes(LocalDate al) {
        return CalculadoraFinanciera.interesMora(montoFinDeMes(), teaMora, diasMoraFinDeMes(al));
    }

    public BigDecimal totalFinDeMes(LocalDate al) {
        return montoFinDeMes().add(moraFinDeMes(al));
    }

    public long diasMoraCuota(Cuota cuota, LocalDate al) {
        return cuota.estaPendiente() ? CalculadoraFinanciera.dias(cuota.getFechaVencimiento(), al) : 0;
    }

    public BigDecimal moraCuota(Cuota cuota, LocalDate al) {
        return CalculadoraFinanciera.interesMora(cuota.getCuota(), teaMora, diasMoraCuota(cuota, al));
    }

    public Optional<Cuota> siguienteCuotaPendiente() {
        return cuotas.stream().filter(Cuota::estaPendiente).findFirst();
    }

    /** ¿Tiene algún importe vencido (con días de mora) a la fecha indicada? */
    public boolean tieneMora(LocalDate al) {
        if (!estaPendiente()) return false;
        return modalidad == Modalidad.FIN_DE_MES
                ? diasMoraFinDeMes(al) > 0
                : cuotas.stream().anyMatch(c -> diasMoraCuota(c, al) > 0);
    }

    /**
     * Capital pendiente para el límite de crédito: el capital de una obligación a fin de mes, o la suma de las
     * amortizaciones de las cuotas no pagadas.
     */
    public BigDecimal capitalPendiente() {
        if (!estaPendiente()) return Redondeo.dinero(BigDecimal.ZERO);
        if (modalidad == Modalidad.FIN_DE_MES) return capital;
        return Redondeo.dinero(cuotas.stream().filter(Cuota::estaPendiente).map(Cuota::getAmortizacion)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    // ---------- Operaciones ----------

    /** Cancela una obligación a fin de mes. El pago es total: mora, interés compensatorio y capital. */
    public Pago pagarFinDeMes(LocalDate fechaPagoReal) {
        exigirPendiente();
        if (modalidad != Modalidad.FIN_DE_MES)
            throw new IllegalStateException("La obligación es en cuotas: se paga cuota por cuota");
        var pago = new Pago(null, fechaPagoReal, moraFinDeMes(fechaPagoReal), interesCompensatorio, capital);
        pagos.add(pago);
        estado = EstadoDeuda.PAGADA;
        return pago;
    }

    /** Cancela una cuota completa. Las cuotas deben pagarse en orden. */
    public Pago pagarCuota(int numero, LocalDate fechaPagoReal) {
        exigirPendiente();
        if (modalidad != Modalidad.CUOTAS)
            throw new IllegalStateException("La obligación es a fin de mes: se paga completa");
        var siguiente = siguienteCuotaPendiente()
                .orElseThrow(() -> new IllegalStateException("No hay cuotas pendientes"));
        if (siguiente.getNumero() != numero)
            throw new IllegalStateException("Debe pagar primero la cuota %d".formatted(siguiente.getNumero()));
        var pago = new Pago(numero, fechaPagoReal, moraCuota(siguiente, fechaPagoReal), siguiente.getInteres(),
                siguiente.getAmortizacion());
        siguiente.setEstado(EstadoDeuda.PAGADA);
        siguiente.setFechaPagoReal(fechaPagoReal);
        pagos.add(pago);
        if (siguienteCuotaPendiente().isEmpty()) estado = EstadoDeuda.PAGADA;
        return pago;
    }

    /** Condona lo que queda pendiente de la obligación. La deuda no se borra: queda en el historial. */
    public void condonar(String motivo, LocalDate fecha) {
        exigirPendiente();
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("El motivo de la condonación es obligatorio");
        cuotas.stream().filter(Cuota::estaPendiente).forEach(c -> c.setEstado(EstadoDeuda.CONDONADA));
        estado = EstadoDeuda.CONDONADA;
        motivoCondonacion = motivo;
        fechaCondonacion = fecha;
    }

    private void exigirPendiente() {
        if (!estaPendiente()) throw new IllegalStateException("La obligación ya no está pendiente");
    }
}
