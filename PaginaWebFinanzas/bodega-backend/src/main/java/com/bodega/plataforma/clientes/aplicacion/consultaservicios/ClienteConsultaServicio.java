package com.bodega.plataforma.clientes.aplicacion.consultaservicios;

import com.bodega.plataforma.clientes.dominio.modelo.agregados.Cliente;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientePorIdConsulta;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientePorUsuarioIdConsulta;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientesPorTiendaConsulta;

import java.util.List;
import java.util.Optional;

public interface ClienteConsultaServicio {
    Optional<Cliente> manejar(ObtenerClientePorIdConsulta consulta);

    Optional<Cliente> manejar(ObtenerClientePorUsuarioIdConsulta consulta);

    List<Cliente> manejar(ObtenerClientesPorTiendaConsulta consulta);
}
