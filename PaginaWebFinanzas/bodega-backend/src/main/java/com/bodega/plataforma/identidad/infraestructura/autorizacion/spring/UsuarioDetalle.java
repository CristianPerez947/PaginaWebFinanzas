package com.bodega.plataforma.identidad.infraestructura.autorizacion.spring;

import com.bodega.plataforma.compartido.interfaces.seguridad.PrincipalBodega;
import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal de Spring Security: expone usuario, rol y tienda al resto de contextos vía {@link PrincipalBodega}.
 */
public class UsuarioDetalle implements UserDetails, PrincipalBodega {

    private final Long id;
    private final String nombreUsuario;
    private final String clave;
    private final String rol;
    private final Long tiendaId;
    private final boolean activo;

    public UsuarioDetalle(Usuario usuario) {
        this.id = usuario.getId();
        this.nombreUsuario = usuario.getNombreUsuario();
        this.clave = usuario.getClaveHash();
        this.rol = usuario.getRol().name();
        this.tiendaId = usuario.getTiendaId();
        this.activo = usuario.isActivo();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol));
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    @Override
    public Long usuarioId() {
        return id;
    }

    @Override
    public String nombreUsuario() {
        return nombreUsuario;
    }

    @Override
    public String rol() {
        return rol;
    }

    @Override
    public Long tiendaId() {
        return tiendaId;
    }
}
