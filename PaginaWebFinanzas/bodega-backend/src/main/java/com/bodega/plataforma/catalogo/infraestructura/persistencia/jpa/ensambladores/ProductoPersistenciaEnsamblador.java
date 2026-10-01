package com.bodega.plataforma.catalogo.infraestructura.persistencia.jpa.ensambladores;

import com.bodega.plataforma.catalogo.dominio.modelo.agregados.Producto;
import com.bodega.plataforma.catalogo.infraestructura.persistencia.jpa.entidades.ProductoPersistenciaEntidad;

public final class ProductoPersistenciaEnsamblador {

    private ProductoPersistenciaEnsamblador() {
    }

    public static Producto aDominioDesdePersistencia(ProductoPersistenciaEntidad e) {
        if (e == null) return null;
        var p = new Producto();
        p.setId(e.getId());
        p.setTiendaId(e.getTiendaId());
        p.setNombre(e.getNombre());
        p.setCategoria(e.getCategoria());
        p.setMarca(e.getMarca());
        p.setProveedor(e.getProveedor());
        p.setDescripcion(e.getDescripcion());
        p.setUnidadMedida(e.getUnidadMedida());
        p.setImagen(e.getImagen());
        p.setPrecioContado(e.getPrecioContado());
        p.setPrecioCredito(e.getPrecioCredito());
        p.setPermiteCuotas(e.isPermiteCuotas());
        p.setActivo(e.isActivo());
        return p;
    }

    public static ProductoPersistenciaEntidad aPersistenciaDesdeDominio(Producto p) {
        if (p == null) return null;
        var e = new ProductoPersistenciaEntidad();
        if (p.getId() != null) e.setId(p.getId());
        e.setTiendaId(p.getTiendaId());
        e.setNombre(p.getNombre());
        e.setCategoria(p.getCategoria());
        e.setMarca(p.getMarca());
        e.setProveedor(p.getProveedor());
        e.setDescripcion(p.getDescripcion());
        e.setUnidadMedida(p.getUnidadMedida());
        e.setImagen(p.getImagen());
        e.setPrecioContado(p.getPrecioContado());
        e.setPrecioCredito(p.getPrecioCredito());
        e.setPermiteCuotas(p.isPermiteCuotas());
        e.setActivo(p.isActivo());
        return e;
    }
}
