package com.bodega.plataforma.clientes.dominio.repositorios;

import com.bodega.plataforma.clientes.dominio.modelo.agregados.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositorio {
    Optional<Cliente> buscarPorIdYTienda(Long id, Long tiendaId);

    Optional<Cliente> buscarPorUsuarioId(Long usuarioId);

    List<Cliente> buscarPorTienda(Long tiendaId);

    List<Cliente> buscarActivosPorTienda(Long tiendaId);

    boolean existePorTiendaYDni(Long tiendaId, String dni);

    Cliente guardar(Cliente cliente);
}
