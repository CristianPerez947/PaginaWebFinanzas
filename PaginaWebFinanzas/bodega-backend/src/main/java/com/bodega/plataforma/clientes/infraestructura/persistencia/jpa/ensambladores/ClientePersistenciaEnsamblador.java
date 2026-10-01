package com.bodega.plataforma.clientes.infraestructura.persistencia.jpa.ensambladores;

import com.bodega.plataforma.clientes.dominio.modelo.agregados.Cliente;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.CondicionesCredito;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.TasaInteres;
import com.bodega.plataforma.clientes.infraestructura.persistencia.jpa.entidades.ClientePersistenciaEntidad;

public final class ClientePersistenciaEnsamblador {

    private ClientePersistenciaEnsamblador() {
    }

    public static Cliente aDominioDesdePersistencia(ClientePersistenciaEntidad e) {
        if (e == null) return null;
        var cliente = new Cliente();
        cliente.setId(e.getId());
        cliente.setTiendaId(e.getTiendaId());
        cliente.setUsuarioId(e.getUsuarioId());
        cliente.setDni(e.getDni());
        cliente.setNombres(e.getNombres());
        cliente.setApellidos(e.getApellidos());
        cliente.setTelefono(e.getTelefono());
        cliente.setDireccion(e.getDireccion());
        cliente.setActivo(e.isActivo());
        cliente.setCondiciones(new CondicionesCredito(e.getMoneda(), e.getLimiteCredito(), e.getMaxMeses(),
                e.getDiaCorte(), e.getHoraCorte(), e.getDiaPago(),
                new TasaInteres(e.getTipoTasa(), e.getTasaCompensatoria(), e.getCapitalizacion()),
                new TasaInteres(e.getTipoTasaMora(), e.getTasaMora(), e.getCapitalizacionMora())));
        return cliente;
    }

    public static ClientePersistenciaEntidad aPersistenciaDesdeDominio(Cliente c) {
        if (c == null) return null;
        var e = new ClientePersistenciaEntidad();
        if (c.getId() != null) e.setId(c.getId());
        e.setTiendaId(c.getTiendaId());
        e.setUsuarioId(c.getUsuarioId());
        e.setDni(c.getDni());
        e.setNombres(c.getNombres());
        e.setApellidos(c.getApellidos());
        e.setTelefono(c.getTelefono());
        e.setDireccion(c.getDireccion());
        e.setActivo(c.isActivo());
        var k = c.getCondiciones();
        e.setMoneda(k.moneda());
        e.setLimiteCredito(k.limiteCredito());
        e.setMaxMeses(k.maxMeses());
        e.setDiaCorte(k.diaCorte());
        e.setHoraCorte(k.horaCorte());
        e.setDiaPago(k.diaPago());
        e.setTipoTasa(k.tasaCompensatoria().tipo());
        e.setTasaCompensatoria(k.tasaCompensatoria().porcentaje());
        e.setCapitalizacion(k.tasaCompensatoria().capitalizacion());
        e.setTipoTasaMora(k.tasaMoratoria().tipo());
        e.setTasaMora(k.tasaMoratoria().porcentaje());
        e.setCapitalizacionMora(k.tasaMoratoria().capitalizacion());
        return e;
    }
}
