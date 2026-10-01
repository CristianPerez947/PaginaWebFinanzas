package com.bodega.plataforma.estadocuenta.interfaces.rest.transformacion;

import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.MensajeWhatsApp;
import com.bodega.plataforma.estadocuenta.interfaces.rest.recursos.EstadoCuentaRecursos.EstadoCuentaRecurso;
import com.bodega.plataforma.estadocuenta.interfaces.rest.recursos.EstadoCuentaRecursos.LineaEstadoCuentaRecurso;
import com.bodega.plataforma.estadocuenta.interfaces.rest.recursos.EstadoCuentaRecursos.MensajeWhatsAppRecurso;

public final class EstadoCuentaRecursoEnsamblador {

    private EstadoCuentaRecursoEnsamblador() {
    }

    public static EstadoCuentaRecurso aRecurso(EstadoCuenta e, String nombreCliente, String dniCliente) {
        var lineas = e.getLineas().stream()
                .map(l -> new LineaEstadoCuentaRecurso(l.getTipo(), l.getFecha(), l.getDescripcion(), l.getMonto(),
                        l.isIncluidaEnTotal()))
                .toList();
        return new EstadoCuentaRecurso(e.getId(), e.getClienteId(), nombreCliente, dniCliente, e.getMoneda(),
                e.getFechaCorte(), e.getFechaPago(), e.getFechaGeneracion(), e.getTotalCompras(), e.getTotalIntereses(),
                e.getTotalCuotas(), e.getTotalMora(), e.getTotalAPagar(), e.getEstado(), lineas);
    }

    public static MensajeWhatsAppRecurso aRecurso(MensajeWhatsApp m) {
        return new MensajeWhatsAppRecurso(m.getId(), m.getClienteId(), m.getNombreCliente(), m.getTelefono(),
                m.getTipo(), m.getContenido(), m.getFechaEnvio(), m.getEstadoEnvio());
    }
}
