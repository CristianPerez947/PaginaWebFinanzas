package com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.repositorios;

import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.EstadoEstadoCuenta;
import com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.entidades.EstadoCuentaPersistenciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EstadoCuentaPersistenciaRepositorio extends JpaRepository<EstadoCuentaPersistenciaEntidad, Long> {
    Optional<EstadoCuentaPersistenciaEntidad> findByIdAndTiendaId(Long id, Long tiendaId);

    Optional<EstadoCuentaPersistenciaEntidad> findByTiendaIdAndClienteIdAndFechaCorte(Long tiendaId, Long clienteId,
                                                                                      LocalDate fechaCorte);

    List<EstadoCuentaPersistenciaEntidad> findByTiendaIdOrderByFechaCorteDescIdDesc(Long tiendaId);

    List<EstadoCuentaPersistenciaEntidad> findByTiendaIdAndClienteIdOrderByFechaCorteDesc(Long tiendaId, Long clienteId);

    List<EstadoCuentaPersistenciaEntidad> findByTiendaIdAndEstado(Long tiendaId, EstadoEstadoCuenta estado);
}
