package com.bodega.plataforma.identidad.infraestructura.autorizacion.spring;

import com.bodega.plataforma.identidad.aplicacion.interno.serviciossalientes.tokens.ServicioToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que lee el token Bearer, lo valida y carga al usuario (activo) en el contexto de seguridad.
 */
@Slf4j
public class FiltroSolicitudBearer extends OncePerRequestFilter {

    private final ServicioToken servicioToken;
    private final UserDetailsService detalleUsuarioServicio;

    public FiltroSolicitudBearer(ServicioToken servicioToken, UserDetailsService detalleUsuarioServicio) {
        this.servicioToken = servicioToken;
        this.detalleUsuarioServicio = detalleUsuarioServicio;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest solicitud, @NonNull HttpServletResponse respuesta,
                                    @NonNull FilterChain cadena) throws ServletException, IOException {
        try {
            var token = servicioToken.obtenerTokenBearerDe(solicitud);
            if (token != null && servicioToken.validarToken(token)) {
                var nombreUsuario = servicioToken.obtenerNombreUsuarioDelToken(token);
                var detalle = detalleUsuarioServicio.loadUserByUsername(nombreUsuario);
                if (detalle.isEnabled()) {
                    var autenticacion = new UsernamePasswordAuthenticationToken(detalle, null, detalle.getAuthorities());
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(solicitud));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo establecer la autenticación: {}", e.getMessage());
        }
        cadena.doFilter(solicitud, respuesta);
    }
}
