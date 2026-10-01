package com.bodega.plataforma.credito.interfaces.acl;

import com.bodega.plataforma.credito.aplicacion.consultaservicios.ObligacionConsultaServicio;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerObligacionesPorClienteConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerObligacionesPorTiendaConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerVentasPorTiendaConsulta;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fachada ACL del contexto de crédito para estados de cuenta y analítica (solo lectura).
 * Devuelve el modelo de dominio de crédito ya calculado; los otros contextos no recalculan fórmulas.
 */
@Service
public class CreditoContextoFachada {

    private final ObligacionConsultaServicio consultaServicio;

    public CreditoContextoFachada(ObligacionConsultaServicio consultaServicio) {
        this.consultaServicio = consultaServicio;
    }

    public List<Obligacion> obligacionesDeCliente(Long tiendaId, Long clienteId) {
        return consultaServicio.manejar(new ObtenerObligacionesPorClienteConsulta(tiendaId, clienteId));
    }

    public List<Obligacion> obligacionesDeTienda(Long tiendaId) {
        return consultaServicio.manejar(new ObtenerObligacionesPorTiendaConsulta(tiendaId));
    }

    public List<VentaCredito> ventasDeCliente(Long tiendaId, Long clienteId) {
        return consultaServicio.manejar(new com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerVentasPorClienteConsulta(tiendaId, clienteId));
    }

    public List<VentaCredito> ventasDeTienda(Long tiendaId) {
        return consultaServicio.manejar(new ObtenerVentasPorTiendaConsulta(tiendaId));
    }
}
