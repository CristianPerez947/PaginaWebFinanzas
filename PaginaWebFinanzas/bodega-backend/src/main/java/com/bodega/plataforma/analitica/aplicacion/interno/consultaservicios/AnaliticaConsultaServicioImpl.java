package com.bodega.plataforma.analitica.aplicacion.interno.consultaservicios;

import com.bodega.plataforma.analitica.aplicacion.consultaservicios.AnaliticaConsultaServicio;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaDeudor;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaInteresMensual;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaMoroso;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaProductoVendido;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.PanelVendedor;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.ProximoVencimiento;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.PuntoSerie;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.ResumenCartera;
import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada;
import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada.ClienteParaCredito;
import com.bodega.plataforma.compartido.dominio.servicios.Redondeo;
import com.bodega.plataforma.compartido.dominio.servicios.RelojSistema;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.modelo.entidades.Pago;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.EstadoDeuda;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import com.bodega.plataforma.credito.interfaces.acl.CreditoContextoFachada;
import com.bodega.plataforma.tiendas.interfaces.acl.TiendasContextoFachada;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cálculo en memoria de los indicadores del panel y de los reportes (una tienda maneja pocos miles de
 * registros). Los importes se expresan en soles.
 */
@Service
@Transactional(readOnly = true)
public class AnaliticaConsultaServicioImpl implements AnaliticaConsultaServicio {

    private static final BigDecimal CERO = Redondeo.dinero(BigDecimal.ZERO);

    private final CreditoContextoFachada creditoFachada;
    private final ClientesContextoFachada clientesFachada;
    private final TiendasContextoFachada tiendasFachada;
    private final RelojSistema reloj;

    public AnaliticaConsultaServicioImpl(CreditoContextoFachada creditoFachada, ClientesContextoFachada clientesFachada,
                                         TiendasContextoFachada tiendasFachada, RelojSistema reloj) {
        this.creditoFachada = creditoFachada;
        this.clientesFachada = clientesFachada;
        this.tiendasFachada = tiendasFachada;
        this.reloj = reloj;
    }

    /** Datos de la tienda cargados una vez por consulta. */
    private record Contexto(LocalDate hoy, BigDecimal tipoCambio, List<Obligacion> obligaciones,
                            List<VentaCredito> ventas, Map<Long, ClienteParaCredito> clientes) {

        BigDecimal soles(BigDecimal monto, String moneda) {
            return "USD".equals(moneda) ? Redondeo.dinero(monto.multiply(tipoCambio)) : monto;
        }
    }

    private Contexto cargar(Long tiendaId) {
        var clientes = clientesFachada.listarClientes(tiendaId, false).stream()
                .collect(Collectors.toMap(ClienteParaCredito::id, Function.identity()));
        return new Contexto(reloj.hoy(), tiendasFachada.obtenerTipoCambioUsd(tiendaId).orElse(BigDecimal.ONE),
                creditoFachada.obligacionesDeTienda(tiendaId), creditoFachada.ventasDeTienda(tiendaId), clientes);
    }

    @Override
    public PanelVendedor panel(Long tiendaId) {
        var c = cargar(tiendaId);
        var cartera = resumenCartera(c);
        var deudores = filasDeudores(c);
        var morosos = filasMorosos(c);
        var mesActual = YearMonth.from(c.hoy());
        var ventasDelMes = sumar(c.ventas().stream().filter(v -> YearMonth.from(v.getFechaHora()).equals(mesActual))
                .map(v -> c.soles(v.getTotal(), v.getMoneda())));
        return new PanelVendedor(c.hoy(),
                (int) c.clientes().values().stream().filter(ClienteParaCredito::activo).count(),
                (int) deudores.stream().filter(d -> d.capitalPendiente().signum() > 0).count(),
                morosos.size(), cartera.capitalPendiente(), cartera.deudaVencida(), cartera.interesesPorCobrar(),
                cartera.totalCobrado(), ventasDelMes, serieVentasPorMes(c), serieCobrosPorMes(c),
                serieVentasPorModalidad(c), ventasPorProductoDe(c).stream().limit(6).toList(),
                deudores.stream().filter(d -> d.capitalPendiente().signum() > 0).limit(5).toList(), morosos,
                proximosVencimientos(c));
    }

    @Override
    public List<FilaDeudor> deudores(Long tiendaId) {
        return filasDeudores(cargar(tiendaId));
    }

    @Override
    public List<FilaMoroso> morosos(Long tiendaId) {
        return filasMorosos(cargar(tiendaId));
    }

    @Override
    public List<FilaProductoVendido> ventasPorProducto(Long tiendaId) {
        return ventasPorProductoDe(cargar(tiendaId));
    }

    @Override
    public List<FilaInteresMensual> intereses(Long tiendaId) {
        var c = cargar(tiendaId);
        Map<YearMonth, BigDecimal[]> porMes = new TreeMap<>();
        for (var o : c.obligaciones())
            for (var p : o.getPagos()) {
                var acumulado = porMes.computeIfAbsent(YearMonth.from(p.getFechaPago()), k -> new BigDecimal[]{CERO, CERO});
                acumulado[0] = acumulado[0].add(c.soles(p.getMontoInteres(), o.getMoneda()));
                acumulado[1] = acumulado[1].add(c.soles(p.getMontoMora(), o.getMoneda()));
            }
        var filas = new ArrayList<FilaInteresMensual>();
        porMes.forEach((mes, v) -> filas.add(new FilaInteresMensual(mes.toString(), Redondeo.dinero(v[0]),
                Redondeo.dinero(v[1]), Redondeo.dinero(v[0].add(v[1])))));
        return filas;
    }

    @Override
    public ResumenCartera cartera(Long tiendaId) {
        return resumenCartera(cargar(tiendaId));
    }

    // ---------- Cálculos ----------

    private ResumenCartera resumenCartera(Contexto c) {
        var capital = sumar(c.obligaciones().stream().map(o -> c.soles(o.capitalPendiente(), o.getMoneda())));
        var vencida = sumar(c.obligaciones().stream().map(o -> c.soles(vencido(o, c.hoy()), o.getMoneda())));
        var porCobrar = sumar(c.obligaciones().stream().filter(Obligacion::estaPendiente)
                .map(o -> c.soles(interesPendiente(o), o.getMoneda())));
        var cobrados = sumar(c.obligaciones().stream().flatMap(o -> o.getPagos().stream()
                .map(p -> c.soles(p.getMontoInteres(), o.getMoneda()))));
        var mora = sumar(c.obligaciones().stream().flatMap(o -> o.getPagos().stream()
                .map(p -> c.soles(p.getMontoMora(), o.getMoneda()))));
        var totalCobrado = sumar(c.obligaciones().stream().flatMap(o -> o.getPagos().stream()
                .map(p -> c.soles(p.total(), o.getMoneda()))));
        return new ResumenCartera(capital, vencida, porCobrar, cobrados, mora, totalCobrado,
                serieVentasPorModalidad(c), serieCarteraPorEstado(c));
    }

    private List<PuntoSerie> serieCarteraPorEstado(Contexto c) {
        Map<EstadoDeuda, BigDecimal> montos = new LinkedHashMap<>();
        for (var estado : EstadoDeuda.values()) montos.put(estado, CERO);
        for (var o : c.obligaciones())
            montos.merge(o.getEstado(), c.soles(o.getCapital(), o.getMoneda()), BigDecimal::add);
        return montos.entrySet().stream().map(e -> new PuntoSerie(e.getKey().name(), Redondeo.dinero(e.getValue()))).toList();
    }

    private List<FilaDeudor> filasDeudores(Contexto c) {
        Map<Long, List<Obligacion>> porCliente = c.obligaciones().stream().collect(Collectors.groupingBy(Obligacion::getClienteId));
        var filas = new ArrayList<FilaDeudor>();
        for (var cliente : c.clientes().values()) {
            var os = porCliente.getOrDefault(cliente.id(), List.of());
            var capital = sumar(os.stream().map(o -> c.soles(o.capitalPendiente(), o.getMoneda())));
            if (capital.signum() == 0 && os.stream().noneMatch(Obligacion::estaPendiente)) continue;
            var exigible = sumar(os.stream().map(o -> c.soles(exigible(o, c.hoy()), o.getMoneda())));
            filas.add(new FilaDeudor(cliente.id(), cliente.nombreCompleto(), cliente.dni(), cliente.telefono(), capital,
                    c.soles(cliente.limiteCredito(), cliente.moneda()), exigible,
                    os.stream().anyMatch(o -> o.tieneMora(c.hoy()))));
        }
        filas.sort(Comparator.comparing(FilaDeudor::capitalPendiente).reversed());
        return filas;
    }

    private List<FilaMoroso> filasMorosos(Contexto c) {
        Map<Long, List<Obligacion>> porCliente = c.obligaciones().stream()
                .filter(o -> o.tieneMora(c.hoy())).collect(Collectors.groupingBy(Obligacion::getClienteId));
        var filas = new ArrayList<FilaMoroso>();
        porCliente.forEach((clienteId, os) -> {
            var cliente = c.clientes().get(clienteId);
            if (cliente == null) return;
            long dias = os.stream().mapToLong(o -> diasMora(o, c.hoy())).max().orElse(0);
            var vencido = sumar(os.stream().map(o -> c.soles(vencido(o, c.hoy()), o.getMoneda())));
            var mora = sumar(os.stream().map(o -> c.soles(moraAcumulada(o, c.hoy()), o.getMoneda())));
            filas.add(new FilaMoroso(clienteId, cliente.nombreCompleto(), cliente.telefono(), dias, vencido, mora));
        });
        filas.sort(Comparator.comparingLong(FilaMoroso::diasMoraMaximo).reversed());
        return filas;
    }

    private List<FilaProductoVendido> ventasPorProductoDe(Contexto c) {
        Map<String, BigDecimal[]> porProducto = new LinkedHashMap<>();
        for (var v : c.ventas())
            for (var l : v.getLineas()) {
                var fila = porProducto.computeIfAbsent(l.getNombreProducto(), k -> new BigDecimal[]{BigDecimal.ZERO, CERO});
                fila[0] = fila[0].add(l.getCantidad());
                fila[1] = fila[1].add(c.soles(l.getSubtotal(), v.getMoneda()));
            }
        return porProducto.entrySet().stream()
                .map(e -> new FilaProductoVendido(e.getKey(), e.getValue()[0].stripTrailingZeros(), Redondeo.dinero(e.getValue()[1])))
                .sorted(Comparator.comparing(FilaProductoVendido::monto).reversed()).toList();
    }

    private List<PuntoSerie> serieVentasPorMes(Contexto c) {
        var meses = ultimosMeses(c.hoy(), 6);
        return meses.stream().map(m -> new PuntoSerie(m.toString(), sumar(c.ventas().stream()
                .filter(v -> YearMonth.from(v.getFechaHora()).equals(m)).map(v -> c.soles(v.getTotal(), v.getMoneda()))))).toList();
    }

    private List<PuntoSerie> serieCobrosPorMes(Contexto c) {
        var meses = ultimosMeses(c.hoy(), 6);
        return meses.stream().map(m -> new PuntoSerie(m.toString(), sumar(c.obligaciones().stream()
                .flatMap(o -> o.getPagos().stream().filter(p -> YearMonth.from(p.getFechaPago()).equals(m))
                        .map(p -> c.soles(p.total(), o.getMoneda())))))).toList();
    }

    private List<PuntoSerie> serieVentasPorModalidad(Contexto c) {
        Map<Modalidad, BigDecimal> montos = new LinkedHashMap<>();
        for (var m : Modalidad.values()) montos.put(m, CERO);
        for (var v : c.ventas())
            for (var l : v.getLineas()) montos.merge(l.getModalidad(), c.soles(l.getSubtotal(), v.getMoneda()), BigDecimal::add);
        return montos.entrySet().stream().map(e -> new PuntoSerie(e.getKey().name(), Redondeo.dinero(e.getValue()))).toList();
    }

    private List<ProximoVencimiento> proximosVencimientos(Contexto c) {
        var hasta = c.hoy().plusDays(15);
        var lista = new ArrayList<ProximoVencimiento>();
        for (var o : c.obligaciones()) {
            if (!o.estaPendiente()) continue;
            var cliente = c.clientes().get(o.getClienteId());
            if (cliente == null) continue;
            if (o.getModalidad() == Modalidad.FIN_DE_MES) {
                if (!o.getFechaPago().isBefore(c.hoy()) && !o.getFechaPago().isAfter(hasta))
                    lista.add(new ProximoVencimiento(cliente.id(), cliente.nombreCompleto(), o.getFechaPago(),
                            c.soles(o.montoFinDeMes(), o.getMoneda()), "Compra a fin de mes"));
            } else {
                o.siguienteCuotaPendiente().ifPresent(cuota -> {
                    if (!cuota.getFechaVencimiento().isBefore(c.hoy()) && !cuota.getFechaVencimiento().isAfter(hasta))
                        lista.add(new ProximoVencimiento(cliente.id(), cliente.nombreCompleto(), cuota.getFechaVencimiento(),
                                c.soles(cuota.getCuota(), o.getMoneda()), "Cuota %d/%d".formatted(cuota.getNumero(), o.getNumCuotas())));
                });
            }
        }
        lista.sort(Comparator.comparing(ProximoVencimiento::fecha));
        return lista;
    }

    // ---------- Auxiliares ----------

    private static List<YearMonth> ultimosMeses(LocalDate hoy, int cantidad) {
        var meses = new ArrayList<YearMonth>();
        var actual = YearMonth.from(hoy);
        for (int i = cantidad - 1; i >= 0; i--) meses.add(actual.minusMonths(i));
        return meses;
    }

    private static BigDecimal sumar(java.util.stream.Stream<BigDecimal> montos) {
        return Redondeo.dinero(montos.reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    /** Intereses compensatorios aún no cobrados de una obligación pendiente. */
    private static BigDecimal interesPendiente(Obligacion o) {
        if (o.getModalidad() == Modalidad.FIN_DE_MES) return o.getInteresCompensatorio();
        return o.getCuotas().stream().filter(cu -> cu.estaPendiente()).map(cu -> cu.getInteres())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Importe vencido (con mora) a la fecha. */
    private static BigDecimal vencido(Obligacion o, LocalDate hoy) {
        if (!o.estaPendiente()) return BigDecimal.ZERO;
        if (o.getModalidad() == Modalidad.FIN_DE_MES) return o.diasMoraFinDeMes(hoy) > 0 ? o.totalFinDeMes(hoy) : BigDecimal.ZERO;
        return o.getCuotas().stream().filter(cu -> o.diasMoraCuota(cu, hoy) > 0)
                .map(cu -> cu.getCuota().add(o.moraCuota(cu, hoy))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Lo que el cliente debe pagar hoy por la obligación: lo vencido más lo que vence hoy. */
    private static BigDecimal exigible(Obligacion o, LocalDate hoy) {
        if (!o.estaPendiente()) return BigDecimal.ZERO;
        if (o.getModalidad() == Modalidad.FIN_DE_MES)
            return o.getFechaPago().isAfter(hoy) ? BigDecimal.ZERO : o.totalFinDeMes(hoy);
        return o.getCuotas().stream().filter(cu -> cu.estaPendiente() && !cu.getFechaVencimiento().isAfter(hoy))
                .map(cu -> cu.getCuota().add(o.moraCuota(cu, hoy))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal moraAcumulada(Obligacion o, LocalDate hoy) {
        if (o.getModalidad() == Modalidad.FIN_DE_MES) return o.moraFinDeMes(hoy);
        return o.getCuotas().stream().map(cu -> o.moraCuota(cu, hoy)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static long diasMora(Obligacion o, LocalDate hoy) {
        if (o.getModalidad() == Modalidad.FIN_DE_MES) return o.diasMoraFinDeMes(hoy);
        return o.getCuotas().stream().mapToLong(cu -> o.diasMoraCuota(cu, hoy)).max().orElse(0);
    }
}
