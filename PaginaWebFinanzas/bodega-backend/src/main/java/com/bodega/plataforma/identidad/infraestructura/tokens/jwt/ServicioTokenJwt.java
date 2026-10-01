package com.bodega.plataforma.identidad.infraestructura.tokens.jwt;

import com.bodega.plataforma.identidad.aplicacion.interno.serviciossalientes.tokens.ServicioToken;
import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * Emisión y validación de tokens JWT firmados con HMAC.
 */
@Slf4j
@Service
public class ServicioTokenJwt implements ServicioToken {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final String secreto;
    private final int diasExpiracion;

    public ServicioTokenJwt(@Value("${autorizacion.jwt.secreto}") String secreto,
                            @Value("${autorizacion.jwt.expiracion.dias}") int diasExpiracion) {
        this.secreto = secreto;
        this.diasExpiracion = diasExpiracion;
    }

    @Override
    public String generarToken(Usuario usuario) {
        var emitido = new Date();
        var expira = new Date(emitido.getTime() + Duration.ofDays(diasExpiracion).toMillis());
        var constructor = Jwts.builder()
                .subject(usuario.getNombreUsuario())
                .claim("rol", usuario.getRol().name())
                .issuedAt(emitido)
                .expiration(expira);
        if (usuario.getTiendaId() != null) constructor.claim("tiendaId", usuario.getTiendaId());
        return constructor.signWith(clave()).compact();
    }

    @Override
    public String obtenerNombreUsuarioDelToken(String token) {
        return Jwts.parser().verifyWith(clave()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    @Override
    public boolean validarToken(String token) {
        try {
            Jwts.parser().verifyWith(clave()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String obtenerTokenBearerDe(HttpServletRequest solicitud) {
        var encabezado = solicitud.getHeader("Authorization");
        if (StringUtils.hasText(encabezado) && encabezado.startsWith(PREFIJO_BEARER))
            return encabezado.substring(PREFIJO_BEARER.length());
        return null;
    }

    private SecretKey clave() {
        return Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }
}
