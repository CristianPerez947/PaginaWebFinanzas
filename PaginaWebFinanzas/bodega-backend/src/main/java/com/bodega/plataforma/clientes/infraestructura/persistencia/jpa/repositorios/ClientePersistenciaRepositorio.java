package com.bodega.plataforma.clientes.infraestructura.persistencia.jpa.repositorios;

import com.bodega.plataforma.clientes.infraestructura.persistencia.jpa.entidades.ClientePersistenciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientePersistenciaRepositorio extends JpaRepository<ClientePersistenciaEntidad, Long> {
    Optional<ClientePersistenciaEntidad> findByIdAndTiendaId(Long id, Long tiendaId);

    Optional<ClientePersistenciaEntidad> findByUsuarioId(Long usuarioId);

    List<ClientePersistenciaEntidad> findByTiendaIdOrderByApellidosAscNombresAsc(Long tiendaId);

    List<ClientePersistenciaEntidad> findByTiendaIdAndActivoTrueOrderByApellidosAscNombresAsc(Long tiendaId);

    boolean existsByTiendaIdAndDni(Long tiendaId, String dni);
}
