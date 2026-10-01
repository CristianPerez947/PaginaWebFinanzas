package com.bodega.plataforma.identidad.infraestructura.autorizacion.spring;

import com.bodega.plataforma.identidad.aplicacion.consultaservicios.UsuarioConsultaServicio;
import com.bodega.plataforma.identidad.dominio.modelo.consultas.ObtenerUsuarioPorNombreConsulta;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("detalleUsuarioServicio")
public class DetalleUsuarioServicio implements UserDetailsService {

    private final UsuarioConsultaServicio usuarioConsultaServicio;

    public DetalleUsuarioServicio(UsuarioConsultaServicio usuarioConsultaServicio) {
        this.usuarioConsultaServicio = usuarioConsultaServicio;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        var usuario = usuarioConsultaServicio.manejar(new ObtenerUsuarioPorNombreConsulta(nombreUsuario))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + nombreUsuario));
        return new UsuarioDetalle(usuario);
    }
}
