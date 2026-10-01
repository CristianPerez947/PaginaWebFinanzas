package com.bodega.plataforma.identidad.infraestructura.autorizacion.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 401 con un cuerpo JSON cuando se accede sin credenciales válidas.
 */
@Component
public class PuntoEntradaNoAutorizado implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest solicitud, HttpServletResponse respuesta,
                         AuthenticationException excepcion) throws IOException {
        respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.getWriter().write(
                "{\"codigo\":\"NO_AUTORIZADO\",\"mensaje\":\"Sesión no válida o expirada\",\"detalle\":\"Inicie sesión nuevamente\"}");
    }
}
