package com.bodega.plataforma.tiendas.aplicacion.interno.comandoservicios;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.identidad.interfaces.acl.IdentidadContextoFachada;
import com.bodega.plataforma.tiendas.aplicacion.comandoservicios.TiendaComandoServicio;
import com.bodega.plataforma.tiendas.dominio.modelo.agregados.Tienda;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.ActualizarTiendaComando;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.CambiarEstadoTiendaComando;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.CrearTiendaComando;
import com.bodega.plataforma.tiendas.dominio.modelo.eventos.TiendaEstadoCambiadoEvento;
import com.bodega.plataforma.tiendas.dominio.repositorios.TiendaRepositorio;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * Servicio de aplicación que ejecuta los comandos sobre tiendas.
 */
@Service
@Transactional
public class TiendaComandoServicioImpl implements TiendaComandoServicio {

    private final TiendaRepositorio tiendaRepositorio;
    private final IdentidadContextoFachada identidadFachada;
    private final ApplicationEventPublisher publicadorEventos;

    public TiendaComandoServicioImpl(TiendaRepositorio tiendaRepositorio, IdentidadContextoFachada identidadFachada,
                                     ApplicationEventPublisher publicadorEventos) {
        this.tiendaRepositorio = tiendaRepositorio;
        this.identidadFachada = identidadFachada;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public Resultado<Tienda, ErrorAplicacion> manejar(CrearTiendaComando comando) {
        if (tiendaRepositorio.existePorSlug(comando.slug()))
            return Resultado.fallo(ErrorAplicacion.conflicto("Tienda",
                    "El identificador de URL '%s' ya está en uso".formatted(comando.slug())));
        var tienda = tiendaRepositorio.guardar(new Tienda(comando));
        var usuario = identidadFachada.crearVendedor(comando.usuarioVendedor(), comando.claveVendedor(), tienda.getId());
        if (usuario instanceof Resultado.Fallo<Long, ErrorAplicacion> fallo) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Resultado.fallo(fallo.error());
        }
        tienda.setVendedorUsuarioId(((Resultado.Exito<Long, ErrorAplicacion>) usuario).valor());
        tienda = tiendaRepositorio.guardar(tienda);
        publicadorEventos.publishEvent(new TiendaEstadoCambiadoEvento(tienda.getId(), "CREAR_TIENDA",
                "Tienda '%s' creada".formatted(tienda.getNombre())));
        return Resultado.exito(tienda);
    }

    @Override
    public Resultado<Tienda, ErrorAplicacion> manejar(ActualizarTiendaComando comando) {
        var tienda = tiendaRepositorio.buscarPorId(comando.tiendaId());
        if (tienda.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Tienda", comando.tiendaId().toString()));
        var actualizada = tiendaRepositorio.guardar(tienda.get().actualizarInformacion(comando.nombre(), comando.logo(),
                comando.ruc(), comando.direccion(), comando.telefono(), comando.tipoCambioUsd(), comando.nombreDueno()));
        publicadorEventos.publishEvent(new TiendaEstadoCambiadoEvento(actualizada.getId(), "ACTUALIZAR_TIENDA",
                "Tienda '%s' actualizada".formatted(actualizada.getNombre())));
        return Resultado.exito(actualizada);
    }

    @Override
    public Resultado<Tienda, ErrorAplicacion> manejar(CambiarEstadoTiendaComando comando) {
        var tienda = tiendaRepositorio.buscarPorId(comando.tiendaId());
        if (tienda.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Tienda", comando.tiendaId().toString()));
        tienda.get().cambiarEstado(comando.activa());
        var guardada = tiendaRepositorio.guardar(tienda.get());
        identidadFachada.cambiarEstadoUsuariosDeTienda(guardada.getId(), comando.activa());
        publicadorEventos.publishEvent(new TiendaEstadoCambiadoEvento(guardada.getId(),
                comando.activa() ? "REACTIVAR_TIENDA" : "DAR_DE_BAJA_TIENDA",
                "Tienda '%s' %s".formatted(guardada.getNombre(), comando.activa() ? "reactivada" : "dada de baja")));
        return Resultado.exito(guardada);
    }
}
