package com.bodega.plataforma.tiendas.infraestructura.persistencia.jpa.repositorios;

import com.bodega.plataforma.tiendas.infraestructura.persistencia.jpa.entidades.TiendaPersistenciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TiendaPersistenciaRepositorio extends JpaRepository<TiendaPersistenciaEntidad, Long> {
    Optional<TiendaPersistenciaEntidad> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<TiendaPersistenciaEntidad> findByActivaTrueOrderByNombreAsc();
}
