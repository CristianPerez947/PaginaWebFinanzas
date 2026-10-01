package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.repositorios;

import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.EstadoDeuda;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades.ObligacionPersistenciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ObligacionPersistenciaRepositorio extends JpaRepository<ObligacionPersistenciaEntidad, Long> {
    Optional<ObligacionPersistenciaEntidad> findByIdAndTiendaId(Long id, Long tiendaId);

    List<ObligacionPersistenciaEntidad> findByTiendaIdAndClienteIdOrderByFechaCompraAsc(Long tiendaId, Long clienteId);

    List<ObligacionPersistenciaEntidad> findByTiendaIdAndClienteIdAndEstadoOrderByFechaCompraAsc(Long tiendaId,
                                                                                                Long clienteId,
                                                                                                EstadoDeuda estado);

    List<ObligacionPersistenciaEntidad> findByTiendaIdOrderByFechaCompraAsc(Long tiendaId);
}
