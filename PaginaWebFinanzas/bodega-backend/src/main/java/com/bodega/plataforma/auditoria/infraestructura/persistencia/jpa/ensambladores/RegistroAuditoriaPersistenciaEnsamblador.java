package com.bodega.plataforma.auditoria.infraestructura.persistencia.jpa.ensambladores;

import com.bodega.plataforma.auditoria.dominio.modelo.agregados.RegistroAuditoria;
import com.bodega.plataforma.auditoria.infraestructura.persistencia.jpa.entidades.RegistroAuditoriaPersistenciaEntidad;

public final class RegistroAuditoriaPersistenciaEnsamblador {

    private RegistroAuditoriaPersistenciaEnsamblador() {
    }

    public static RegistroAuditoria aDominioDesdePersistencia(RegistroAuditoriaPersistenciaEntidad e) {
        if (e == null) return null;
        var r = new RegistroAuditoria();
        r.setId(e.getId());
        r.setTiendaId(e.getTiendaId());
        r.setUsuario(e.getUsuario());
        r.setRol(e.getRol());
        r.setAccion(e.getAccion());
        r.setEntidad(e.getEntidad());
        r.setEntidadId(e.getEntidadId());
        r.setDetalle(e.getDetalle());
        r.setFechaHora(e.getFechaHora());
        return r;
    }

    public static RegistroAuditoriaPersistenciaEntidad aPersistenciaDesdeDominio(RegistroAuditoria r) {
        if (r == null) return null;
        var e = new RegistroAuditoriaPersistenciaEntidad();
        if (r.getId() != null) e.setId(r.getId());
        e.setTiendaId(r.getTiendaId());
        e.setUsuario(r.getUsuario());
        e.setRol(r.getRol());
        e.setAccion(r.getAccion());
        e.setEntidad(r.getEntidad());
        e.setEntidadId(r.getEntidadId());
        e.setDetalle(r.getDetalle() == null ? null : r.getDetalle().substring(0, Math.min(600, r.getDetalle().length())));
        e.setFechaHora(r.getFechaHora());
        return e;
    }
}
