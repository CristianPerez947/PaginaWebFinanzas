package com.bodega.plataforma.identidad.dominio.modelo.comandos;

import com.bodega.plataforma.identidad.dominio.modelo.objetosvalor.Rol;

/**
 * Comando para crear un usuario.
 *
 * @param nombreUsuario nombre de acceso, no vacío
 * @param clave         clave en texto plano (mínimo 6 caracteres); se guarda cifrada
 * @param rol           rol del usuario
 * @param tiendaId      tienda del usuario (nulo solo para el administrador del sistema)
 */
public record CrearUsuarioComando(String nombreUsuario, String clave, Rol rol, Long tiendaId) {
    public CrearUsuarioComando {
        if (nombreUsuario == null || nombreUsuario.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        if (clave == null || clave.length() < 6)
            throw new IllegalArgumentException("La clave debe tener al menos 6 caracteres");
        if (rol == null)
            throw new IllegalArgumentException("El rol es obligatorio");
    }
}
