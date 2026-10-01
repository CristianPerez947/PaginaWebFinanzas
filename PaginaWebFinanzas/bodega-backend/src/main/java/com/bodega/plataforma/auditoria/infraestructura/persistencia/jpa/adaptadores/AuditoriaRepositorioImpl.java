package com.bodega.plataforma.auditoria.infraestructura.persistencia.jpa.adaptadores;

import com.bodega.plataforma.auditoria.dominio.modelo.agregados.RegistroAuditoria;
import com.bodega.plataforma.auditoria.dominio.repositorios.AuditoriaRepositorio;
import com.bodega.plataforma.auditoria.infraestructura.persistencia.jpa.ensambladores.RegistroAuditoriaPersistenciaEnsamblador;
import com.bodega.plataforma.auditoria.infraestructura.persistencia.jpa.repositorios.RegistroAuditoriaPersistenciaRepositorio;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditoriaRepositorioImpl implements AuditoriaRepositorio {

    private final RegistroAuditoriaPersistenciaRepositorio repositorio;

    public AuditoriaRepositorioImpl(RegistroAuditoriaPersistenciaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public RegistroAuditoria guardar(RegistroAuditoria registro) {
        var guardado = repositorio.save(RegistroAuditoriaPersistenciaEnsamblador.aPersistenciaDesdeDominio(registro));
        return RegistroAuditoriaPersistenciaEnsamblador.aDominioDesdePersistencia(guardado);
    }

    @Override
    public List<RegistroAuditoria> buscarRecientes(int limite) {
        return repositorio.findAllByOrderByFechaHoraDescIdDesc(PageRequest.of(0, limite)).stream()
                .map(RegistroAuditoriaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public List<RegistroAuditoria> buscarRecientesPorTienda(Long tiendaId, int limite) {
        return repositorio.findByTiendaIdOrderByFechaHoraDescIdDesc(tiendaId, PageRequest.of(0, limite)).stream()
                .map(RegistroAuditoriaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }
}
