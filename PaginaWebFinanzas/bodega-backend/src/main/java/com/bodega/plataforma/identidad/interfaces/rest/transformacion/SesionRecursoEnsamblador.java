package com.bodega.plataforma.identidad.interfaces.rest.transformacion;

import com.bodega.plataforma.identidad.aplicacion.comandoservicios.UsuarioComandoServicio.SesionIniciada;
import com.bodega.plataforma.identidad.interfaces.rest.recursos.SesionRecurso;

public final class SesionRecursoEnsamblador {

    private SesionRecursoEnsamblador() {
    }

    public static SesionRecurso aRecursoDesdeEntidad(SesionIniciada sesion) {
        var usuario = sesion.usuario();
        return new SesionRecurso(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol().name(),
                usuario.getTiendaId(), sesion.token());
    }
}
