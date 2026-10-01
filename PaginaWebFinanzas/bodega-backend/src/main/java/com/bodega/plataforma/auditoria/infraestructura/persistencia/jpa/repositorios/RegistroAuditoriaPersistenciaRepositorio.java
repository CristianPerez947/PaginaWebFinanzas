package com.bodega.plataforma.auditoria.infraestructura.persistencia.jpa.repositorios;

import com.bodega.plataforma.auditoria.infraestructura.persistencia.jpa.entidades.RegistroAuditoriaPersistenciaEntidad;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroAuditoriaPersistenciaRepositorio extends JpaRepository<RegistroAuditoriaPersistenciaEntidad, Long> {
    List<RegistroAuditoriaPersistenciaEntidad> findAllByOrderByFechaHoraDescIdDesc(Pageable pagina);

    List<RegistroAuditoriaPersistenciaEntidad> findByTiendaIdOrderByFechaHoraDescIdDesc(Long tiendaId, Pageable pagina);
}
