package com.bodega.plataforma.tiendas.dominio.repositorios;

import com.bodega.plataforma.tiendas.dominio.modelo.agregados.Tienda;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio de tiendas.
 */
public interface TiendaRepositorio {
    Optional<Tienda> buscarPorId(Long id);

    Optional<Tienda> buscarPorSlug(String slug);

    List<Tienda> buscarTodas();

    List<Tienda> buscarActivas();

    boolean existePorSlug(String slug);

    Tienda guardar(Tienda tienda);
}
