package com.bodega.plataforma.catalogo.aplicacion.interno.consultaservicios;

import com.bodega.plataforma.catalogo.aplicacion.consultaservicios.ProductoConsultaServicio;
import com.bodega.plataforma.catalogo.dominio.modelo.agregados.Producto;
import com.bodega.plataforma.catalogo.dominio.modelo.consultas.ObtenerProductoPorIdConsulta;
import com.bodega.plataforma.catalogo.dominio.modelo.consultas.ObtenerProductosPorTiendaConsulta;
import com.bodega.plataforma.catalogo.dominio.repositorios.ProductoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductoConsultaServicioImpl implements ProductoConsultaServicio {

    private final ProductoRepositorio productoRepositorio;

    public ProductoConsultaServicioImpl(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    @Override
    public Optional<Producto> manejar(ObtenerProductoPorIdConsulta consulta) {
        return productoRepositorio.buscarPorIdYTienda(consulta.productoId(), consulta.tiendaId());
    }

    @Override
    public List<Producto> manejar(ObtenerProductosPorTiendaConsulta consulta) {
        return consulta.soloActivos()
                ? productoRepositorio.buscarActivosPorTienda(consulta.tiendaId())
                : productoRepositorio.buscarPorTienda(consulta.tiendaId());
    }
}
