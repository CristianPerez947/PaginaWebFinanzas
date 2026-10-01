package com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.ensambladores;

import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.MensajeWhatsApp;
import com.bodega.plataforma.estadocuenta.dominio.modelo.entidades.LineaEstadoCuenta;
import com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.entidades.EstadoCuentaPersistenciaEntidad;
import com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.entidades.LineaEstadoCuentaPersistenciaEntidad;
import com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.entidades.MensajeWhatsAppPersistenciaEntidad;

import java.util.ArrayList;

public final class EstadoCuentaPersistenciaEnsamblador {

    private EstadoCuentaPersistenciaEnsamblador() {
    }

    public static EstadoCuenta aDominioDesdePersistencia(EstadoCuentaPersistenciaEntidad e) {
        if (e == null) return null;
        var d = new EstadoCuenta();
        d.setId(e.getId());
        d.setTiendaId(e.getTiendaId());
        d.setClienteId(e.getClienteId());
        d.setMoneda(e.getMoneda());
        d.setFechaCorte(e.getFechaCorte());
        d.setFechaPago(e.getFechaPago());
        d.setFechaGeneracion(e.getFechaGeneracion());
        d.setTotalCompras(e.getTotalCompras());
        d.setTotalIntereses(e.getTotalIntereses());
        d.setTotalCuotas(e.getTotalCuotas());
        d.setTotalMora(e.getTotalMora());
        d.setTotalAPagar(e.getTotalAPagar());
        d.setEstado(e.getEstado());
        var lineas = new ArrayList<LineaEstadoCuenta>();
        for (var l : e.getLineas()) {
            var linea = new LineaEstadoCuenta(l.getTipo(), l.getFecha(), l.getDescripcion(), l.getMonto(),
                    l.isIncluidaEnTotal());
            linea.setId(l.getId());
            lineas.add(linea);
        }
        d.setLineas(lineas);
        return d;
    }

    /** Las líneas se recrean en cada guardado (las anteriores se eliminan por orphanRemoval). */
    public static EstadoCuentaPersistenciaEntidad aPersistenciaDesdeDominio(EstadoCuenta d) {
        if (d == null) return null;
        var e = new EstadoCuentaPersistenciaEntidad();
        if (d.getId() != null) e.setId(d.getId());
        e.setTiendaId(d.getTiendaId());
        e.setClienteId(d.getClienteId());
        e.setMoneda(d.getMoneda());
        e.setFechaCorte(d.getFechaCorte());
        e.setFechaPago(d.getFechaPago());
        e.setFechaGeneracion(d.getFechaGeneracion());
        e.setTotalCompras(d.getTotalCompras());
        e.setTotalIntereses(d.getTotalIntereses());
        e.setTotalCuotas(d.getTotalCuotas());
        e.setTotalMora(d.getTotalMora());
        e.setTotalAPagar(d.getTotalAPagar());
        e.setEstado(d.getEstado());
        for (var l : d.getLineas()) {
            var le = new LineaEstadoCuentaPersistenciaEntidad();
            if (l.getId() != null) le.setId(l.getId());
            le.setEstadoCuenta(e);
            le.setTipo(l.getTipo());
            le.setFecha(l.getFecha());
            le.setDescripcion(l.getDescripcion().substring(0, Math.min(400, l.getDescripcion().length())));
            le.setMonto(l.getMonto());
            le.setIncluidaEnTotal(l.isIncluidaEnTotal());
            e.getLineas().add(le);
        }
        return e;
    }

    public static MensajeWhatsApp aDominioDesdePersistencia(MensajeWhatsAppPersistenciaEntidad e) {
        var m = new MensajeWhatsApp();
        m.setId(e.getId());
        m.setTiendaId(e.getTiendaId());
        m.setClienteId(e.getClienteId());
        m.setNombreCliente(e.getNombreCliente());
        m.setTelefono(e.getTelefono());
        m.setTipo(e.getTipo());
        m.setReferenciaId(e.getReferenciaId());
        m.setContenido(e.getContenido());
        m.setFechaEnvio(e.getFechaEnvio());
        m.setEstadoEnvio(e.getEstadoEnvio());
        return m;
    }

    public static MensajeWhatsAppPersistenciaEntidad aPersistenciaDesdeDominio(MensajeWhatsApp m) {
        var e = new MensajeWhatsAppPersistenciaEntidad();
        if (m.getId() != null) e.setId(m.getId());
        e.setTiendaId(m.getTiendaId());
        e.setClienteId(m.getClienteId());
        e.setNombreCliente(m.getNombreCliente());
        e.setTelefono(m.getTelefono());
        e.setTipo(m.getTipo());
        e.setReferenciaId(m.getReferenciaId());
        e.setContenido(m.getContenido().substring(0, Math.min(500, m.getContenido().length())));
        e.setFechaEnvio(m.getFechaEnvio());
        e.setEstadoEnvio(m.getEstadoEnvio());
        return e;
    }
}
