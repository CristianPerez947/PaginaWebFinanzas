package com.bodega.plataforma.clientes.aplicacion.interno.consultaservicios;

import com.bodega.plataforma.clientes.aplicacion.consultaservicios.ClienteConsultaServicio;
import com.bodega.plataforma.clientes.dominio.modelo.agregados.Cliente;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientePorIdConsulta;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientePorUsuarioIdConsulta;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientesPorTiendaConsulta;
import com.bodega.plataforma.clientes.dominio.repositorios.ClienteRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ClienteConsultaServicioImpl implements ClienteConsultaServicio {

    private final ClienteRepositorio clienteRepositorio;

    public ClienteConsultaServicioImpl(ClienteRepositorio clienteRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
    }

    @Override
    public Optional<Cliente> manejar(ObtenerClientePorIdConsulta consulta) {
        return clienteRepositorio.buscarPorIdYTienda(consulta.clienteId(), consulta.tiendaId());
    }

    @Override
    public Optional<Cliente> manejar(ObtenerClientePorUsuarioIdConsulta consulta) {
        return clienteRepositorio.buscarPorUsuarioId(consulta.usuarioId());
    }

    @Override
    public List<Cliente> manejar(ObtenerClientesPorTiendaConsulta consulta) {
        return consulta.soloActivos()
                ? clienteRepositorio.buscarActivosPorTienda(consulta.tiendaId())
                : clienteRepositorio.buscarPorTienda(consulta.tiendaId());
    }
}
