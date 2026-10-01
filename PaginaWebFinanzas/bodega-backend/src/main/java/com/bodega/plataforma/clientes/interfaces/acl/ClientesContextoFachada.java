package com.bodega.plataforma.clientes.interfaces.acl;

import com.bodega.plataforma.clientes.aplicacion.consultaservicios.ClienteConsultaServicio;
import com.bodega.plataforma.clientes.dominio.modelo.agregados.Cliente;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientePorIdConsulta;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientePorUsuarioIdConsulta;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientesPorTiendaConsulta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Fachada ACL del contexto de clientes. Entrega a otros contextos (crédito, estados de cuenta, analítica)
 * una vista de solo lectura de las condiciones de crédito, sin exponer los tipos internos del dominio.
 */
@Service
public class ClientesContextoFachada {

    /**
     * Cliente y sus condiciones pactadas. Los tipos de tasa, capitalización y moneda se entregan como texto
     * (EFECTIVA/NOMINAL, DIARIA/MENSUAL, PEN/USD); las tasas van en porcentaje anual.
     */
    public record ClienteParaCredito(
            Long id, Long tiendaId, String dni, String nombreCompleto, String telefono, boolean activo,
            String moneda, BigDecimal limiteCredito, int maxMeses, int diaCorte, LocalTime horaCorte, int diaPago,
            String tipoTasa, BigDecimal tasaCompensatoria, String capitalizacion,
            String tipoTasaMora, BigDecimal tasaMora, String capitalizacionMora) {
    }

    private final ClienteConsultaServicio consultaServicio;

    public ClientesContextoFachada(ClienteConsultaServicio consultaServicio) {
        this.consultaServicio = consultaServicio;
    }

    public Optional<ClienteParaCredito> obtenerCliente(Long tiendaId, Long clienteId) {
        return consultaServicio.manejar(new ObtenerClientePorIdConsulta(tiendaId, clienteId)).map(this::aVista);
    }

    public Optional<ClienteParaCredito> obtenerClientePorUsuario(Long usuarioId) {
        return consultaServicio.manejar(new ObtenerClientePorUsuarioIdConsulta(usuarioId)).map(this::aVista);
    }

    public List<ClienteParaCredito> listarClientes(Long tiendaId, boolean soloActivos) {
        return consultaServicio.manejar(new ObtenerClientesPorTiendaConsulta(tiendaId, soloActivos)).stream()
                .map(this::aVista).toList();
    }

    private ClienteParaCredito aVista(Cliente c) {
        var k = c.getCondiciones();
        return new ClienteParaCredito(c.getId(), c.getTiendaId(), c.getDni(), c.nombreCompleto(), c.getTelefono(),
                c.isActivo(), k.moneda().name(), k.limiteCredito(), k.maxMeses(), k.diaCorte(), k.horaCorte(),
                k.diaPago(), k.tasaCompensatoria().tipo().name(), k.tasaCompensatoria().porcentaje(),
                k.tasaCompensatoria().capitalizacion() == null ? null : k.tasaCompensatoria().capitalizacion().name(),
                k.tasaMoratoria().tipo().name(), k.tasaMoratoria().porcentaje(),
                k.tasaMoratoria().capitalizacion() == null ? null : k.tasaMoratoria().capitalizacion().name());
    }
}
