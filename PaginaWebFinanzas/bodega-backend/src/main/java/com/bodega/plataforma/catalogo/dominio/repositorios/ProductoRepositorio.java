package com.bodega.plataforma.catalogo.dominio.repositorios;

import com.bodega.plataforma.catalogo.dominio.modelo.agregados.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositorio {
    Optional<Producto> buscarPorIdYTienda(Long id, Long tiendaId);

    List<Producto> buscarPorTienda(Long tiendaId);

    List<Producto> buscarActivosPorTienda(Long tiendaId);

    Producto guardar(Producto producto);
}
