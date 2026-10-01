package com.bodega.plataforma.credito.aplicacion.interno.consultaservicios;

import com.bodega.plataforma.credito.aplicacion.consultaservicios.ObligacionConsultaServicio;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerObligacionPorIdConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerObligacionesPorClienteConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerObligacionesPorTiendaConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerVentasPorClienteConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerVentasPorTiendaConsulta;
import com.bodega.plataforma.credito.dominio.repositorios.ObligacionRepositorio;
import com.bodega.plataforma.credito.dominio.repositorios.VentaCreditoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ObligacionConsultaServicioImpl implements ObligacionConsultaServicio {

    private final ObligacionRepositorio obligacionRepositorio;
    private final VentaCreditoRepositorio ventaRepositorio;

    public ObligacionConsultaServicioImpl(ObligacionRepositorio obligacionRepositorio,
                                          VentaCreditoRepositorio ventaRepositorio) {
        this.obligacionRepositorio = obligacionRepositorio;
        this.ventaRepositorio = ventaRepositorio;
    }

    @Override
    public Optional<Obligacion> manejar(ObtenerObligacionPorIdConsulta c) {
        return obligacionRepositorio.buscarPorIdYTienda(c.obligacionId(), c.tiendaId());
    }

    @Override
    public List<Obligacion> manejar(ObtenerObligacionesPorClienteConsulta c) {
        return obligacionRepositorio.buscarPorCliente(c.tiendaId(), c.clienteId());
    }

    @Override
    public List<Obligacion> manejar(ObtenerObligacionesPorTiendaConsulta c) {
        return obligacionRepositorio.buscarPorTienda(c.tiendaId());
    }

    @Override
    public List<VentaCredito> manejar(ObtenerVentasPorClienteConsulta c) {
        return ventaRepositorio.buscarPorCliente(c.tiendaId(), c.clienteId());
    }

    @Override
    public List<VentaCredito> manejar(ObtenerVentasPorTiendaConsulta c) {
        return ventaRepositorio.buscarPorTienda(c.tiendaId());
    }
}
