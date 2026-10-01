package com.bodega.plataforma.identidad.aplicacion.comandoservicios;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CambiarClaveComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CambiarEstadoUsuarioComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CambiarEstadoUsuariosDeTiendaComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CrearUsuarioComando;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.IniciarSesionComando;

/**
 * Casos de uso de escritura sobre usuarios.
 */
public interface UsuarioComandoServicio {

    /** Usuario autenticado junto con su token de acceso. */
    record SesionIniciada(Usuario usuario, String token) {
    }

    Resultado<Usuario, ErrorAplicacion> manejar(CrearUsuarioComando comando);

    Resultado<SesionIniciada, ErrorAplicacion> manejar(IniciarSesionComando comando);

    Resultado<Usuario, ErrorAplicacion> manejar(CambiarClaveComando comando);

    Resultado<Usuario, ErrorAplicacion> manejar(CambiarEstadoUsuarioComando comando);

    Resultado<Integer, ErrorAplicacion> manejar(CambiarEstadoUsuariosDeTiendaComando comando);
}
