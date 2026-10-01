package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.ensambladores;

import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.modelo.entidades.LineaVenta;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades.LineaVentaPersistenciaEntidad;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades.VentaPersistenciaEntidad;

import java.util.ArrayList;

public final class VentaPersistenciaEnsamblador {

    private VentaPersistenciaEnsamblador() {
    }

    public static VentaCredito aDominioDesdePersistencia(VentaPersistenciaEntidad e) {
        if (e == null) return null;
        var v = new VentaCredito();
        v.setId(e.getId());
        v.setTiendaId(e.getTiendaId());
        v.setClienteId(e.getClienteId());
        v.setFechaHora(e.getFechaHora());
        v.setMoneda(e.getMoneda());
        v.setTipoCambioAplicado(e.getTipoCambioAplicado());
        v.setTotal(e.getTotal());
        var lineas = new ArrayList<LineaVenta>();
        for (var l : e.getLineas()) {
            var linea = new LineaVenta(l.getProductoId(), l.getNombreProducto(), l.getCantidad(), l.getPrecioUnitario(),
                    l.getSubtotal(), l.getModalidad(), l.getNumCuotas());
            linea.setId(l.getId());
            lineas.add(linea);
        }
        v.setLineas(lineas);
        return v;
    }

    public static VentaPersistenciaEntidad aPersistenciaDesdeDominio(VentaCredito v) {
        if (v == null) return null;
        var e = new VentaPersistenciaEntidad();
        if (v.getId() != null) e.setId(v.getId());
        e.setTiendaId(v.getTiendaId());
        e.setClienteId(v.getClienteId());
        e.setFechaHora(v.getFechaHora());
        e.setMoneda(v.getMoneda());
        e.setTipoCambioAplicado(v.getTipoCambioAplicado());
        e.setTotal(v.getTotal());
        for (var l : v.getLineas()) {
            var le = new LineaVentaPersistenciaEntidad();
            if (l.getId() != null) le.setId(l.getId());
            le.setVenta(e);
            le.setProductoId(l.getProductoId());
            le.setNombreProducto(l.getNombreProducto());
            le.setCantidad(l.getCantidad());
            le.setPrecioUnitario(l.getPrecioUnitario());
            le.setSubtotal(l.getSubtotal());
            le.setModalidad(l.getModalidad());
            le.setNumCuotas(l.getNumCuotas());
            e.getLineas().add(le);
        }
        return e;
    }
}
