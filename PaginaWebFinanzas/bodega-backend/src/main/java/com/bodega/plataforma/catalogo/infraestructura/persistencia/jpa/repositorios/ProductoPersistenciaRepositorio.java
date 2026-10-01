package com.bodega.plataforma.catalogo.infraestructura.persistencia.jpa.repositorios;

import com.bodega.plataforma.catalogo.infraestructura.persistencia.jpa.entidades.ProductoPersistenciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoPersistenciaRepositorio extends JpaRepository<ProductoPersistenciaEntidad, Long> {
    Optional<ProductoPersistenciaEntidad> findByIdAndTiendaId(Long id, Long tiendaId);

    List<ProductoPersistenciaEntidad> findByTiendaIdOrderByCategoriaAscNombreAsc(Long tiendaId);

    List<ProductoPersistenciaEntidad> findByTiendaIdAndActivoTrueOrderByCategoriaAscNombreAsc(Long tiendaId);
}
