package com.bodega.plataforma.tiendas.aplicacion.interno.consultaservicios;

import com.bodega.plataforma.tiendas.aplicacion.consultaservicios.TiendaConsultaServicio;
import com.bodega.plataforma.tiendas.dominio.modelo.agregados.Tienda;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTiendaPorIdConsulta;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTiendaPorSlugConsulta;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTodasLasTiendasConsulta;
import com.bodega.plataforma.tiendas.dominio.repositorios.TiendaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TiendaConsultaServicioImpl implements TiendaConsultaServicio {

    private final TiendaRepositorio tiendaRepositorio;

    public TiendaConsultaServicioImpl(TiendaRepositorio tiendaRepositorio) {
        this.tiendaRepositorio = tiendaRepositorio;
    }

    @Override
    public Optional<Tienda> manejar(ObtenerTiendaPorIdConsulta consulta) {
        return tiendaRepositorio.buscarPorId(consulta.tiendaId());
    }

    @Override
    public Optional<Tienda> manejar(ObtenerTiendaPorSlugConsulta consulta) {
        return tiendaRepositorio.buscarPorSlug(consulta.slug());
    }

    @Override
    public List<Tienda> manejar(ObtenerTodasLasTiendasConsulta consulta) {
        return consulta.soloActivas() ? tiendaRepositorio.buscarActivas() : tiendaRepositorio.buscarTodas();
    }
}
