package com.bodega.plataforma.clientes.aplicacion.interno.comandoservicios;

import com.bodega.plataforma.clientes.aplicacion.comandoservicios.ClienteComandoServicio;
import com.bodega.plataforma.clientes.dominio.modelo.agregados.Cliente;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.ActualizarClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.CambiarEstadoClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.CrearClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.RegenerarClaveClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.eventos.ClienteCambiadoEvento;
import com.bodega.plataforma.clientes.dominio.repositorios.ClienteRepositorio;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.identidad.interfaces.acl.IdentidadContextoFachada;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.security.SecureRandom;

/**
 * Servicio de aplicación que ejecuta los comandos sobre clientes. La clave inicial la genera el sistema
 * (6 dígitos), se devuelve una sola vez y se guarda cifrada en identidad.
 */
@Service
@Transactional
public class ClienteComandoServicioImpl implements ClienteComandoServicio {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final ClienteRepositorio clienteRepositorio;
    private final IdentidadContextoFachada identidadFachada;
    private final ApplicationEventPublisher publicadorEventos;

    public ClienteComandoServicioImpl(ClienteRepositorio clienteRepositorio, IdentidadContextoFachada identidadFachada,
                                      ApplicationEventPublisher publicadorEventos) {
        this.clienteRepositorio = clienteRepositorio;
        this.identidadFachada = identidadFachada;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public Resultado<ClienteConClave, ErrorAplicacion> manejar(CrearClienteComando comando) {
        if (clienteRepositorio.existePorTiendaYDni(comando.tiendaId(), comando.dni()))
            return Resultado.fallo(ErrorAplicacion.conflicto("Cliente",
                    "Ya existe un cliente con el DNI %s en esta tienda".formatted(comando.dni())));
        var clave = generarClave();
        var usuario = identidadFachada.crearCliente(comando.tiendaId(), comando.dni(), clave);
        if (usuario instanceof Resultado.Fallo<Long, ErrorAplicacion> fallo) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Resultado.fallo(fallo.error());
        }
        var cliente = new Cliente(comando);
        cliente.setUsuarioId(((Resultado.Exito<Long, ErrorAplicacion>) usuario).valor());
        cliente = clienteRepositorio.guardar(cliente);
        publicar(cliente, "CREAR_CLIENTE", "Cliente %s (DNI %s) creado".formatted(cliente.nombreCompleto(), cliente.getDni()));
        return Resultado.exito(new ClienteConClave(cliente, clave));
    }

    @Override
    public Resultado<Cliente, ErrorAplicacion> manejar(ActualizarClienteComando c) {
        var cliente = clienteRepositorio.buscarPorIdYTienda(c.clienteId(), c.tiendaId());
        if (cliente.isEmpty()) return Resultado.fallo(ErrorAplicacion.noEncontrado("Cliente", c.clienteId().toString()));
        var actualizado = clienteRepositorio.guardar(cliente.get()
                .actualizarInformacion(c.nombres(), c.apellidos(), c.telefono(), c.direccion(), c.condiciones()));
        publicar(actualizado, "ACTUALIZAR_CLIENTE", "Cliente %s actualizado".formatted(actualizado.nombreCompleto()));
        return Resultado.exito(actualizado);
    }

    @Override
    public Resultado<Cliente, ErrorAplicacion> manejar(CambiarEstadoClienteComando c) {
        var cliente = clienteRepositorio.buscarPorIdYTienda(c.clienteId(), c.tiendaId());
        if (cliente.isEmpty()) return Resultado.fallo(ErrorAplicacion.noEncontrado("Cliente", c.clienteId().toString()));
        cliente.get().cambiarEstado(c.activo());
        var guardado = clienteRepositorio.guardar(cliente.get());
        identidadFachada.cambiarEstadoUsuario(guardado.getUsuarioId(), c.activo());
        publicar(guardado, c.activo() ? "REACTIVAR_CLIENTE" : "DAR_DE_BAJA_CLIENTE",
                "Cliente %s %s".formatted(guardado.nombreCompleto(), c.activo() ? "reactivado" : "dado de baja"));
        return Resultado.exito(guardado);
    }

    @Override
    public Resultado<ClienteConClave, ErrorAplicacion> manejar(RegenerarClaveClienteComando c) {
        var cliente = clienteRepositorio.buscarPorIdYTienda(c.clienteId(), c.tiendaId());
        if (cliente.isEmpty()) return Resultado.fallo(ErrorAplicacion.noEncontrado("Cliente", c.clienteId().toString()));
        var clave = generarClave();
        var resultado = identidadFachada.cambiarClave(cliente.get().getUsuarioId(), clave);
        if (resultado instanceof Resultado.Fallo<Long, ErrorAplicacion> fallo) return Resultado.fallo(fallo.error());
        publicar(cliente.get(), "REGENERAR_CLAVE_CLIENTE",
                "Se regeneró la clave del cliente %s".formatted(cliente.get().nombreCompleto()));
        return Resultado.exito(new ClienteConClave(cliente.get(), clave));
    }

    private String generarClave() {
        return "%06d".formatted(ALEATORIO.nextInt(1_000_000));
    }

    private void publicar(Cliente cliente, String accion, String detalle) {
        publicadorEventos.publishEvent(new ClienteCambiadoEvento(cliente.getTiendaId(), cliente.getId(), accion, detalle));
    }
}
