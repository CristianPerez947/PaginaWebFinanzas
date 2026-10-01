package com.bodega.plataforma.clientes.interfaces.rest.transformacion;

import com.bodega.plataforma.clientes.aplicacion.comandoservicios.ClienteComandoServicio.ClienteConClave;
import com.bodega.plataforma.clientes.dominio.modelo.agregados.Cliente;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.ActualizarClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.CrearClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.CondicionesCredito;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.TasaInteres;
import com.bodega.plataforma.clientes.interfaces.rest.recursos.ClienteConClaveRecurso;
import com.bodega.plataforma.clientes.interfaces.rest.recursos.ClienteRecurso;
import com.bodega.plataforma.clientes.interfaces.rest.recursos.GuardarClienteRecurso;

import java.time.LocalTime;

public final class ClienteRecursoEnsamblador {

    private ClienteRecursoEnsamblador() {
    }

    public static ClienteRecurso aRecursoDesdeEntidad(Cliente c) {
        var k = c.getCondiciones();
        return new ClienteRecurso(c.getId(), c.getDni(), c.getNombres(), c.getApellidos(), c.getTelefono(),
                c.getDireccion(), c.isActivo(), k.moneda(), k.limiteCredito(), k.maxMeses(), k.diaCorte(),
                k.horaCorte().toString(), k.diaPago(), k.tasaCompensatoria().tipo(), k.tasaCompensatoria().porcentaje(),
                k.tasaCompensatoria().capitalizacion(), k.tasaMoratoria().tipo(), k.tasaMoratoria().porcentaje(),
                k.tasaMoratoria().capitalizacion());
    }

    public static ClienteConClaveRecurso aRecursoConClaveDesdeEntidad(ClienteConClave c) {
        return new ClienteConClaveRecurso(aRecursoDesdeEntidad(c.cliente()), c.claveInicial());
    }

    public static CrearClienteComando aComandoCrearDesdeRecurso(Long tiendaId, GuardarClienteRecurso r) {
        return new CrearClienteComando(tiendaId, r.dni(), r.nombres(), r.apellidos(), r.telefono(), r.direccion(),
                aCondiciones(r));
    }

    public static ActualizarClienteComando aComandoActualizarDesdeRecurso(Long tiendaId, Long clienteId,
                                                                          GuardarClienteRecurso r) {
        return new ActualizarClienteComando(tiendaId, clienteId, r.nombres(), r.apellidos(), r.telefono(),
                r.direccion(), aCondiciones(r));
    }

    private static CondicionesCredito aCondiciones(GuardarClienteRecurso r) {
        var hora = r.horaCorte() == null || r.horaCorte().isBlank() ? LocalTime.of(23, 59) : LocalTime.parse(r.horaCorte());
        return new CondicionesCredito(r.moneda(), r.limiteCredito(), r.maxMeses(), r.diaCorte(), hora, r.diaPago(),
                new TasaInteres(r.tipoTasa(), r.tasaCompensatoria(), r.capitalizacion()),
                new TasaInteres(r.tipoTasaMora(), r.tasaMora(), r.capitalizacionMora()));
    }
}
