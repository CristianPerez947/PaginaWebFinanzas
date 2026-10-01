package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.repositorios;

import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades.VentaPersistenciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaPersistenciaRepositorio extends JpaRepository<VentaPersistenciaEntidad, Long> {
    List<VentaPersistenciaEntidad> findByTiendaIdAndClienteIdOrderByFechaHoraDesc(Long tiendaId, Long clienteId);

    List<VentaPersistenciaEntidad> findByTiendaIdOrderByFechaHoraDesc(Long tiendaId);
}
