package com.bodega.plataforma.identidad.infraestructura.persistencia.jpa.ensambladores;

import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import com.bodega.plataforma.identidad.infraestructura.persistencia.jpa.entidades.UsuarioPersistenciaEntidad;

/**
 * Ensamblador estático entre el agregado Usuario y su entidad de persistencia.
 */
public final class UsuarioPersistenciaEnsamblador {

    private UsuarioPersistenciaEnsamblador() {
    }

    public static Usuario aDominioDesdePersistencia(UsuarioPersistenciaEntidad entidad) {
        if (entidad == null) return null;
        var usuario = new Usuario();
        usuario.setId(entidad.getId());
        usuario.setNombreUsuario(entidad.getNombreUsuario());
        usuario.setClaveHash(entidad.getClaveHash());
        usuario.setRol(entidad.getRol());
        usuario.setTiendaId(entidad.getTiendaId());
        usuario.setActivo(entidad.isActivo());
        return usuario;
    }

    public static UsuarioPersistenciaEntidad aPersistenciaDesdeDominio(Usuario usuario) {
        if (usuario == null) return null;
        var entidad = new UsuarioPersistenciaEntidad();
        if (usuario.getId() != null) entidad.setId(usuario.getId());
        entidad.setNombreUsuario(usuario.getNombreUsuario());
        entidad.setClaveHash(usuario.getClaveHash());
        entidad.setRol(usuario.getRol());
        entidad.setTiendaId(usuario.getTiendaId());
        entidad.setActivo(usuario.isActivo());
        return entidad;
    }
}
