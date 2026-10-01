package com.bodega.plataforma.identidad.infraestructura.autorizacion.spring;

import com.bodega.plataforma.identidad.aplicacion.interno.serviciossalientes.tokens.ServicioToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * Seguridad web: API sin estado con JWT. Rutas públicas: autenticación, catálogo público y documentación.
 */
@Configuration
@EnableMethodSecurity
public class ConfiguracionSeguridadWeb {

    private final ServicioToken servicioToken;
    private final UserDetailsService detalleUsuarioServicio;
    private final AuthenticationEntryPoint puntoEntradaNoAutorizado;
    private final List<String> origenesPermitidos;

    public ConfiguracionSeguridadWeb(ServicioToken servicioToken,
                                     @Qualifier("detalleUsuarioServicio") UserDetailsService detalleUsuarioServicio,
                                     AuthenticationEntryPoint puntoEntradaNoAutorizado,
                                     @Value("${cors.origenes-permitidos}") String origenesPermitidos) {
        this.servicioToken = servicioToken;
        this.detalleUsuarioServicio = detalleUsuarioServicio;
        this.puntoEntradaNoAutorizado = puntoEntradaNoAutorizado;
        this.origenesPermitidos = Arrays.stream(origenesPermitidos.split(",")).map(String::trim).toList();
    }

    @Bean
    public FiltroSolicitudBearer filtroSolicitudBearer() {
        return new FiltroSolicitudBearer(servicioToken, detalleUsuarioServicio);
    }

    @Bean
    public SecurityFilterChain cadenaFiltros(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(_ -> {
            var configuracion = new CorsConfiguration();
            configuracion.setAllowedOrigins(origenesPermitidos);
            configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            configuracion.setAllowedHeaders(List.of("*"));
            return configuracion;
        }));
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(e -> e.authenticationEntryPoint(puntoEntradaNoAutorizado))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/autenticacion/**",
                                "/api/v1/publico/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**").permitAll()
                        .anyRequest().authenticated());
        http.addFilterBefore(filtroSolicitudBearer(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
