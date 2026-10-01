package com.bodega.plataforma.tiendas.infraestructura.persistencia.jpa.ensambladores;

import com.bodega.plataforma.tiendas.dominio.modelo.agregados.Tienda;
import com.bodega.plataforma.tiendas.infraestructura.persistencia.jpa.entidades.TiendaPersistenciaEntidad;

public final class TiendaPersistenciaEnsamblador {

    private TiendaPersistenciaEnsamblador() {
    }

    public static Tienda aDominioDesdePersistencia(TiendaPersistenciaEntidad entidad) {
        if (entidad == null) return null;
        var tienda = new Tienda();
        tienda.setId(entidad.getId());
        tienda.setNombre(entidad.getNombre());
        tienda.setSlug(entidad.getSlug());
        tienda.setLogo(entidad.getLogo());
        tienda.setRuc(entidad.getRuc());
        tienda.setDireccion(entidad.getDireccion());
        tienda.setTelefono(entidad.getTelefono());
        tienda.setTipoCambioUsd(entidad.getTipoCambioUsd());
        tienda.setActiva(entidad.isActiva());
        tienda.setNombreDueno(entidad.getNombreDueno());
        tienda.setVendedorUsuarioId(entidad.getVendedorUsuarioId());
        tienda.setUsuarioVendedor(entidad.getUsuarioVendedor());
        return tienda;
    }

    public static TiendaPersistenciaEntidad aPersistenciaDesdeDominio(Tienda tienda) {
        if (tienda == null) return null;
        var entidad = new TiendaPersistenciaEntidad();
        if (tienda.getId() != null) entidad.setId(tienda.getId());
        entidad.setNombre(tienda.getNombre());
        entidad.setSlug(tienda.getSlug());
        entidad.setLogo(tienda.getLogo());
        entidad.setRuc(tienda.getRuc());
        entidad.setDireccion(tienda.getDireccion());
        entidad.setTelefono(tienda.getTelefono());
        entidad.setTipoCambioUsd(tienda.getTipoCambioUsd());
        entidad.setActiva(tienda.isActiva());
        entidad.setNombreDueno(tienda.getNombreDueno());
        entidad.setVendedorUsuarioId(tienda.getVendedorUsuarioId());
        entidad.setUsuarioVendedor(tienda.getUsuarioVendedor());
        return entidad;
    }
}
