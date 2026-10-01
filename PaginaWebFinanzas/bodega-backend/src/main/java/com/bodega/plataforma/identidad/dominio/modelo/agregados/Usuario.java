package com.bodega.plataforma.identidad.dominio.modelo.agregados;

import com.bodega.plataforma.compartido.dominio.modelo.agregados.RaizAgregadoDominio;
import com.bodega.plataforma.identidad.dominio.modelo.objetosvalor.Rol;
import lombok.Getter;
import lombok.Setter;

/**
 * Raíz de agregado Usuario: credenciales y rol de quien accede al sistema.
 * El administrador del sistema no pertenece a una tienda (tiendaId nulo).
 */
@Getter
public class Usuario extends RaizAgregadoDominio<Usuario> {

    @Setter
    private Long id;

    @Setter
    private String nombreUsuario;

    @Setter
    private String claveHash;

    @Setter
    private Rol rol;

    @Setter
    private Long tiendaId;

    @Setter
    private boolean activo = true;

    public Usuario() {
    }

    public Usuario(String nombreUsuario, String claveHash, Rol rol, Long tiendaId) {
        if (nombreUsuario == null || nombreUsuario.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        if (claveHash == null || claveHash.isBlank())
            throw new IllegalArgumentException("La clave es obligatoria");
        if (rol == null)
            throw new IllegalArgumentException("El rol es obligatorio");
        if (rol != Rol.ADMIN_SISTEMA && tiendaId == null)
            throw new IllegalArgumentException("El usuario debe pertenecer a una tienda");
        this.nombreUsuario = nombreUsuario;
        this.claveHash = claveHash;
        this.rol = rol;
        this.tiendaId = tiendaId;
        this.activo = true;
    }

    public void cambiarClave(String nuevaClaveHash) {
        if (nuevaClaveHash == null || nuevaClaveHash.isBlank())
            throw new IllegalArgumentException("La clave es obligatoria");
        this.claveHash = nuevaClaveHash;
    }

    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }

    /**
     * Nombre de usuario interno de un cliente. Incluye la tienda para que un mismo DNI
     * pueda ser cliente de varias tiendas con claves distintas.
     */
    public static String nombreParaCliente(Long tiendaId, String dni) {
        return "c%d:%s".formatted(tiendaId, dni);
    }
}
