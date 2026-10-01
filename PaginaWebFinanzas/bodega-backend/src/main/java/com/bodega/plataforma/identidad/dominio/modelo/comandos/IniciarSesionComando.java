package com.bodega.plataforma.identidad.dominio.modelo.comandos;

import com.bodega.plataforma.identidad.dominio.modelo.objetosvalor.Rol;

/**
 * Comando para iniciar sesión. El rol esperado evita que un cliente use el acceso del personal y viceversa.
 */
public record IniciarSesionComando(String nombreUsuario, String clave, boolean esCliente) {
    public IniciarSesionComando {
        if (nombreUsuario == null || nombreUsuario.isBlank())
            throw new IllegalArgumentException("El usuario es obligatorio");
        if (clave == null || clave.isBlank())
            throw new IllegalArgumentException("La clave es obligatoria");
    }

    public boolean rolPermitido(Rol rol) {
        return esCliente ? rol == Rol.CLIENTE : rol != Rol.CLIENTE;
    }
}
