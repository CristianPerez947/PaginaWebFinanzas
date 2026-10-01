package com.bodega.plataforma.compartido.interfaces.seguridad;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Acceso al usuario autenticado. La tienda siempre se toma del token, nunca de parámetros del cliente.
 */
public final class UsuarioActual {

    public static final String ADMIN_SISTEMA = "ADMIN_SISTEMA";
    public static final String VENDEDOR = "VENDEDOR";
    public static final String CLIENTE = "CLIENTE";

    private UsuarioActual() {
    }

    public static PrincipalBodega obtener() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof PrincipalBodega principal)) {
            throw new IllegalStateException("No hay un usuario autenticado");
        }
        return principal;
    }

    public static Long tiendaId() {
        var tiendaId = obtener().tiendaId();
        if (tiendaId == null) {
            throw new IllegalStateException("El usuario no pertenece a una tienda");
        }
        return tiendaId;
    }
}
