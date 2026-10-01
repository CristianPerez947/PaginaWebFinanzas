package com.bodega.plataforma.tiendas.aplicacion.consultaservicios;

import com.bodega.plataforma.tiendas.dominio.modelo.agregados.Tienda;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTiendaPorIdConsulta;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTiendaPorSlugConsulta;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTodasLasTiendasConsulta;

import java.util.List;
import java.util.Optional;

public interface TiendaConsultaServicio {
    Optional<Tienda> manejar(ObtenerTiendaPorIdConsulta consulta);

    Optional<Tienda> manejar(ObtenerTiendaPorSlugConsulta consulta);

    List<Tienda> manejar(ObtenerTodasLasTiendasConsulta consulta);
}
