package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.ensambladores;

import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.entidades.Cuota;
import com.bodega.plataforma.credito.dominio.modelo.entidades.Pago;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades.CuotaPersistenciaEntidad;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades.ObligacionPersistenciaEntidad;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades.PagoPersistenciaEntidad;

import java.util.ArrayList;

/**
 * Ensamblador estático entre el agregado Obligacion y sus entidades de persistencia.
 */
public final class ObligacionPersistenciaEnsamblador {

    private ObligacionPersistenciaEnsamblador() {
    }

    public static Obligacion aDominioDesdePersistencia(ObligacionPersistenciaEntidad e) {
        if (e == null) return null;
        var o = new Obligacion();
        o.setId(e.getId());
        o.setTiendaId(e.getTiendaId());
        o.setClienteId(e.getClienteId());
        o.setVentaId(e.getVentaId());
        o.setModalidad(e.getModalidad());
        o.setMoneda(e.getMoneda());
        o.setCapital(e.getCapital());
        o.setInteresCompensatorio(e.getInteresCompensatorio());
        o.setFechaCompra(e.getFechaCompra());
        o.setFechaCorte(e.getFechaCorte());
        o.setFechaPago(e.getFechaPago());
        o.setDiasGracia(e.getDiasGracia());
        o.setNumCuotas(e.getNumCuotas());
        o.setTeaCompensatoria(e.getTeaCompensatoria());
        o.setTeaMora(e.getTeaMora());
        o.setEstado(e.getEstado());
        o.setMotivoCondonacion(e.getMotivoCondonacion());
        o.setFechaCondonacion(e.getFechaCondonacion());
        var cuotas = new ArrayList<Cuota>();
        for (var c : e.getCuotas()) {
            var cuota = new Cuota(c.getNumero(), c.getFechaVencimiento(), c.getCuota(), c.getInteres(),
                    c.getAmortizacion(), c.getSaldo(), c.getEstado());
            cuota.setId(c.getId());
            cuota.setFechaPagoReal(c.getFechaPagoReal());
            cuotas.add(cuota);
        }
        o.setCuotas(cuotas);
        var pagos = new ArrayList<Pago>();
        for (var p : e.getPagos()) {
            var pago = new Pago(p.getNumeroCuota(), p.getFechaPago(), p.getMontoMora(), p.getMontoInteres(),
                    p.getMontoCapital());
            pago.setId(p.getId());
            pagos.add(pago);
        }
        o.setPagos(pagos);
        return o;
    }

    public static ObligacionPersistenciaEntidad aPersistenciaDesdeDominio(Obligacion o) {
        if (o == null) return null;
        var e = new ObligacionPersistenciaEntidad();
        if (o.getId() != null) e.setId(o.getId());
        e.setTiendaId(o.getTiendaId());
        e.setClienteId(o.getClienteId());
        e.setVentaId(o.getVentaId());
        e.setModalidad(o.getModalidad());
        e.setMoneda(o.getMoneda());
        e.setCapital(o.getCapital());
        e.setInteresCompensatorio(o.getInteresCompensatorio());
        e.setFechaCompra(o.getFechaCompra());
        e.setFechaCorte(o.getFechaCorte());
        e.setFechaPago(o.getFechaPago());
        e.setDiasGracia(o.getDiasGracia());
        e.setNumCuotas(o.getNumCuotas());
        e.setTeaCompensatoria(o.getTeaCompensatoria());
        e.setTeaMora(o.getTeaMora());
        e.setEstado(o.getEstado());
        e.setMotivoCondonacion(o.getMotivoCondonacion());
        e.setFechaCondonacion(o.getFechaCondonacion());
        for (var c : o.getCuotas()) {
            var ce = new CuotaPersistenciaEntidad();
            if (c.getId() != null) ce.setId(c.getId());
            ce.setObligacion(e);
            ce.setNumero(c.getNumero());
            ce.setFechaVencimiento(c.getFechaVencimiento());
            ce.setCuota(c.getCuota());
            ce.setInteres(c.getInteres());
            ce.setAmortizacion(c.getAmortizacion());
            ce.setSaldo(c.getSaldo());
            ce.setEstado(c.getEstado());
            ce.setFechaPagoReal(c.getFechaPagoReal());
            e.getCuotas().add(ce);
        }
        for (var p : o.getPagos()) {
            var pe = new PagoPersistenciaEntidad();
            if (p.getId() != null) pe.setId(p.getId());
            pe.setObligacion(e);
            pe.setNumeroCuota(p.getNumeroCuota());
            pe.setFechaPago(p.getFechaPago());
            pe.setMontoMora(p.getMontoMora());
            pe.setMontoInteres(p.getMontoInteres());
            pe.setMontoCapital(p.getMontoCapital());
            e.getPagos().add(pe);
        }
        return e;
    }
}
