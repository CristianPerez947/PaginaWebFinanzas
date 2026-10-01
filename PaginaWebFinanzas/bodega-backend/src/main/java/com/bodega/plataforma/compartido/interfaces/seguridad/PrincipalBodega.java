package com.bodega.plataforma.compartido.interfaces.seguridad;

/**
 * Datos del usuario autenticado que necesita cualquier contexto (sin depender de identidad).
 */
public interface PrincipalBodega {
    Long usuarioId();

    String nombreUsuario();

    String rol();

    /** Tienda del usuario; nulo para el administrador del sistema. */
    Long tiendaId();
}
