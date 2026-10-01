package com.bodega.plataforma.identidad.infraestructura.persistencia.jpa.repositorios;

import com.bodega.plataforma.identidad.dominio.modelo.objetosvalor.Rol;
import com.bodega.plataforma.identidad.infraestructura.persistencia.jpa.entidades.UsuarioPersistenciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioPersistenciaRepositorio extends JpaRepository<UsuarioPersistenciaEntidad, Long> {
    Optional<UsuarioPersistenciaEntidad> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByRol(Rol rol);

    List<UsuarioPersistenciaEntidad> findByTiendaId(Long tiendaId);
}
