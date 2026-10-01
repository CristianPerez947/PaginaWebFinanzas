package com.bodega.plataforma.identidad.aplicacion.interno.comandoservicios;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.identidad.aplicacion.comandoservicios.UsuarioComandoServicio;
import com.bodega.plataforma.identidad.aplicacion.interno.serviciossalientes.hashing.ServicioHash;
import com.bodega.plataforma.identidad.aplicacion.interno.serviciossalientes.tokens.ServicioToken;
import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CambiarClaveComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CambiarEstadoUsuarioComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CambiarEstadoUsuariosDeTiendaComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CrearUsuarioComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.IniciarSesionComando;
import com.bodega.plataforma.identidad.dominio.repositorios.UsuarioRepositorio;
import com.bodega.plataforma.identidad.dominio.modelo.eventos.InicioSesionEvento;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que ejecuta los comandos sobre usuarios.
 */
@Service
@Transactional
public class UsuarioComandoServicioImpl implements UsuarioComandoServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final ServicioHash servicioHash;
    private final ServicioToken servicioToken;
    private final ApplicationEventPublisher publicadorEventos;

    public UsuarioComandoServicioImpl(UsuarioRepositorio usuarioRepositorio, ServicioHash servicioHash,
                                      ServicioToken servicioToken, ApplicationEventPublisher publicadorEventos) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.servicioHash = servicioHash;
        this.servicioToken = servicioToken;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public Resultado<Usuario, ErrorAplicacion> manejar(CrearUsuarioComando comando) {
        if (usuarioRepositorio.existePorNombreUsuario(comando.nombreUsuario()))
            return Resultado.fallo(ErrorAplicacion.conflicto("Usuario",
                    "El usuario '%s' ya existe".formatted(comando.nombreUsuario())));
        try {
            var usuario = new Usuario(comando.nombreUsuario(), servicioHash.cifrar(comando.clave()),
                    comando.rol(), comando.tiendaId());
            return Resultado.exito(usuarioRepositorio.guardar(usuario));
        } catch (IllegalArgumentException e) {
            return Resultado.fallo(ErrorAplicacion.validacion("usuario", e.getMessage()));
        }
    }

    @Override
    public Resultado<SesionIniciada, ErrorAplicacion> manejar(IniciarSesionComando comando) {
        var usuario = usuarioRepositorio.buscarPorNombreUsuario(comando.nombreUsuario());
        if (usuario.isEmpty() || !usuario.get().isActivo()
                || !comando.rolPermitido(usuario.get().getRol())
                || !servicioHash.coincide(comando.clave(), usuario.get().getClaveHash()))
            return Resultado.fallo(ErrorAplicacion.noAutorizado("Usuario o clave incorrectos"));
        var token = servicioToken.generarToken(usuario.get());
        publicadorEventos.publishEvent(new InicioSesionEvento(usuario.get().getId(), usuario.get().getNombreUsuario(),
                usuario.get().getRol().name(), usuario.get().getTiendaId()));
        return Resultado.exito(new SesionIniciada(usuario.get(), token));
    }

    @Override
    public Resultado<Usuario, ErrorAplicacion> manejar(CambiarClaveComando comando) {
        var usuario = usuarioRepositorio.buscarPorId(comando.usuarioId());
        if (usuario.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Usuario", comando.usuarioId().toString()));
        usuario.get().cambiarClave(servicioHash.cifrar(comando.nuevaClave()));
        return Resultado.exito(usuarioRepositorio.guardar(usuario.get()));
    }

    @Override
    public Resultado<Usuario, ErrorAplicacion> manejar(CambiarEstadoUsuarioComando comando) {
        var usuario = usuarioRepositorio.buscarPorId(comando.usuarioId());
        if (usuario.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Usuario", comando.usuarioId().toString()));
        usuario.get().cambiarEstado(comando.activo());
        return Resultado.exito(usuarioRepositorio.guardar(usuario.get()));
    }

    @Override
    public Resultado<Integer, ErrorAplicacion> manejar(CambiarEstadoUsuariosDeTiendaComando comando) {
        var usuarios = usuarioRepositorio.buscarPorTienda(comando.tiendaId());
        usuarios.forEach(u -> {
            u.cambiarEstado(comando.activo());
            usuarioRepositorio.guardar(u);
        });
        return Resultado.exito(usuarios.size());
    }
}
