package com.bodega.plataforma.credito.aplicacion.consultaservicios;

import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerObligacionPorIdConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerObligacionesPorClienteConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerObligacionesPorTiendaConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerVentasPorClienteConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerVentasPorTiendaConsulta;

import java.util.List;
import java.util.Optional;

public interface ObligacionConsultaServicio {
    Optional<Obligacion> manejar(ObtenerObligacionPorIdConsulta consulta);

    List<Obligacion> manejar(ObtenerObligacionesPorClienteConsulta consulta);

    List<Obligacion> manejar(ObtenerObligacionesPorTiendaConsulta consulta);

    List<VentaCredito> manejar(ObtenerVentasPorClienteConsulta consulta);

    List<VentaCredito> manejar(ObtenerVentasPorTiendaConsulta consulta);
}
