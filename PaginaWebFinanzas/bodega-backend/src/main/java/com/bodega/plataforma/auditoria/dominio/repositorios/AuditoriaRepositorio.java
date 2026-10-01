package com.bodega.plataforma.auditoria.dominio.repositorios;

import com.bodega.plataforma.auditoria.dominio.modelo.agregados.RegistroAuditoria;

import java.util.List;

public interface AuditoriaRepositorio {
    RegistroAuditoria guardar(RegistroAuditoria registro);

    List<RegistroAuditoria> buscarRecientes(int limite);

    List<RegistroAuditoria> buscarRecientesPorTienda(Long tiendaId, int limite);
}
