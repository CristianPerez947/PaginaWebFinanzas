package com.bodega.plataforma.identidad.interfaces.acl;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.identidad.aplicacion.comandoservicios.UsuarioComandoServicio;
import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CambiarClaveComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CambiarEstadoUsuarioComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CambiarEstadoUsuariosDeTiendaComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CrearUsuarioComando;
import com.bodega.plataforma.identidad.dominio.modelo.objetosvalor.Rol;
import org.springframework.stereotype.Service;

/**
 * Fachada ACL del contexto de identidad. Es la única puerta de entrada para que otros contextos
 * (tiendas, clientes) creen o modifiquen usuarios.
 */
@Service
public class IdentidadContextoFachada {

    private final UsuarioComandoServicio usuarioComandoServicio;

    public IdentidadContextoFachada(UsuarioComandoServicio usuarioComandoServicio) {
        this.usuarioComandoServicio = usuarioComandoServicio;
    }

    /** Crea el usuario de un vendedor. Devuelve el identificador del usuario. */
    public Resultado<Long, ErrorAplicacion> crearVendedor(String nombreUsuario, String clave, Long tiendaId) {
        return usuarioComandoServicio.manejar(new CrearUsuarioComando(nombreUsuario, clave, Rol.VENDEDOR, tiendaId))
                .mapear(Usuario::getId);
    }

    /** Crea el usuario de un cliente (usuario interno tienda + DNI). Devuelve el identificador del usuario. */
    public Resultado<Long, ErrorAplicacion> crearCliente(Long tiendaId, String dni, String clave) {
        return usuarioComandoServicio.manejar(
                new CrearUsuarioComando(Usuario.nombreParaCliente(tiendaId, dni), clave, Rol.CLIENTE, tiendaId))
                .mapear(Usuario::getId);
    }

    public Resultado<Long, ErrorAplicacion> cambiarClave(Long usuarioId, String nuevaClave) {
        return usuarioComandoServicio.manejar(new CambiarClaveComando(usuarioId, nuevaClave)).mapear(Usuario::getId);
    }

    public Resultado<Long, ErrorAplicacion> cambiarEstadoUsuario(Long usuarioId, boolean activo) {
        return usuarioComandoServicio.manejar(new CambiarEstadoUsuarioComando(usuarioId, activo)).mapear(Usuario::getId);
    }

    public void cambiarEstadoUsuariosDeTienda(Long tiendaId, boolean activo) {
        usuarioComandoServicio.manejar(new CambiarEstadoUsuariosDeTiendaComando(tiendaId, activo));
    }
}
