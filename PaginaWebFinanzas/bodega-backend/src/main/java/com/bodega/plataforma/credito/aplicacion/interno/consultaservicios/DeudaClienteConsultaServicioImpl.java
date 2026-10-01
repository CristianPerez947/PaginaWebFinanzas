package com.bodega.plataforma.credito.aplicacion.interno.consultaservicios;

import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.compartido.dominio.servicios.Redondeo;
import com.bodega.plataforma.compartido.dominio.servicios.RelojSistema;
import com.bodega.plataforma.credito.aplicacion.consultaservicios.DeudaClienteConsultaServicio;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import com.bodega.plataforma.credito.dominio.repositorios.ObligacionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class DeudaClienteConsultaServicioImpl implements DeudaClienteConsultaServicio {

    private final ClientesContextoFachada clientesFachada;
    private final ObligacionRepositorio obligacionRepositorio;
    private final RelojSistema reloj;

    public DeudaClienteConsultaServicioImpl(ClientesContextoFachada clientesFachada,
                                            ObligacionRepositorio obligacionRepositorio, RelojSistema reloj) {
        this.clientesFachada = clientesFachada;
        this.obligacionRepositorio = obligacionRepositorio;
        this.reloj = reloj;
    }

    @Override
    public Resultado<DeudaCliente, ErrorAplicacion> obtener(Long tiendaId, Long clienteId) {
        var cliente = clientesFachada.obtenerCliente(tiendaId, clienteId);
        if (cliente.isEmpty()) return Resultado.fallo(ErrorAplicacion.noEncontrado("Cliente", clienteId.toString()));
        var hoy = reloj.hoy();
        var obligaciones = obligacionRepositorio.buscarPorCliente(tiendaId, clienteId);
        var capitalPendiente = Redondeo.dinero(obligaciones.stream().map(Obligacion::capitalPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        var disponible = cliente.get().limiteCredito().subtract(capitalPendiente).max(BigDecimal.ZERO);
        var enMora = obligaciones.stream().anyMatch(o -> o.tieneMora(hoy));
        var totalVencido = Redondeo.dinero(obligaciones.stream().map(o -> vencido(o, hoy))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return Resultado.exito(new DeudaCliente(clienteId, cliente.get().moneda(), cliente.get().limiteCredito(),
                capitalPendiente, Redondeo.dinero(disponible), enMora, totalVencido, hoy, obligaciones));
    }

    /** Importe vencido (con su mora) de una obligación a la fecha indicada. */
    private BigDecimal vencido(Obligacion o, LocalDate hoy) {
        if (!o.estaPendiente()) return BigDecimal.ZERO;
        if (o.getModalidad() == Modalidad.FIN_DE_MES)
            return o.diasMoraFinDeMes(hoy) > 0 ? o.totalFinDeMes(hoy) : BigDecimal.ZERO;
        return o.getCuotas().stream().filter(c -> o.diasMoraCuota(c, hoy) > 0)
                .map(c -> c.getCuota().add(o.moraCuota(c, hoy))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
