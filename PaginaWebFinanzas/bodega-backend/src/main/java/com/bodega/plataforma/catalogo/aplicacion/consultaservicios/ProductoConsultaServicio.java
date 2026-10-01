package com.bodega.plataforma.catalogo.aplicacion.consultaservicios;

import com.bodega.plataforma.catalogo.dominio.modelo.agregados.Producto;
import com.bodega.plataforma.catalogo.dominio.modelo.consultas.ObtenerProductoPorIdConsulta;
import com.bodega.plataforma.catalogo.dominio.modelo.consultas.ObtenerProductosPorTiendaConsulta;

import java.util.List;
import java.util.Optional;

public interface ProductoConsultaServicio {
    Optional<Producto> manejar(ObtenerProductoPorIdConsulta consulta);

    List<Producto> manejar(ObtenerProductosPorTiendaConsulta consulta);
}
