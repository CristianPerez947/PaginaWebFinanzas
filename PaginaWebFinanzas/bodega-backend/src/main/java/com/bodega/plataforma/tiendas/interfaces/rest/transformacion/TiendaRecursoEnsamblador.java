package com.bodega.plataforma.tiendas.interfaces.rest.transformacion;

import com.bodega.plataforma.tiendas.dominio.modelo.agregados.Tienda;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.ActualizarTiendaComando;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.CrearTiendaComando;
import com.bodega.plataforma.tiendas.interfaces.rest.recursos.ActualizarTiendaRecurso;
import com.bodega.plataforma.tiendas.interfaces.rest.recursos.CrearTiendaRecurso;
import com.bodega.plataforma.tiendas.interfaces.rest.recursos.TiendaPublicaRecurso;
import com.bodega.plataforma.tiendas.interfaces.rest.recursos.TiendaRecurso;

public final class TiendaRecursoEnsamblador {

    private TiendaRecursoEnsamblador() {
    }

    public static TiendaRecurso aRecursoDesdeEntidad(Tienda t) {
        return new TiendaRecurso(t.getId(), t.getNombre(), t.getSlug(), t.getLogo(), t.getRuc(), t.getDireccion(),
                t.getTelefono(), t.getTipoCambioUsd(), t.isActiva(), t.getNombreDueno(), t.getUsuarioVendedor());
    }

    public static TiendaPublicaRecurso aRecursoPublicoDesdeEntidad(Tienda t) {
        return new TiendaPublicaRecurso(t.getId(), t.getNombre(), t.getSlug(), t.getLogo(), t.getDireccion(),
                t.getTelefono(), t.getTipoCambioUsd());
    }

    public static CrearTiendaComando aComandoDesdeRecurso(CrearTiendaRecurso r) {
        return new CrearTiendaComando(r.nombre(), r.slug(), r.logo(), r.ruc(), r.direccion(), r.telefono(),
                r.tipoCambioUsd(), r.nombreDueno(), r.usuarioVendedor(), r.claveVendedor());
    }

    public static ActualizarTiendaComando aComandoDesdeRecurso(Long tiendaId, ActualizarTiendaRecurso r) {
        return new ActualizarTiendaComando(tiendaId, r.nombre(), r.logo(), r.ruc(), r.direccion(), r.telefono(),
                r.tipoCambioUsd(), r.nombreDueno());
    }
}
