package com.bodega.plataforma.identidad.aplicacion.interno.serviciossalientes.tokens;

import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Servicio saliente para emitir y validar tokens de acceso.
 */
public interface ServicioToken {
    String generarToken(Usuario usuario);

    String obtenerNombreUsuarioDelToken(String token);

    boolean validarToken(String token);

    String obtenerTokenBearerDe(HttpServletRequest solicitud);
}
