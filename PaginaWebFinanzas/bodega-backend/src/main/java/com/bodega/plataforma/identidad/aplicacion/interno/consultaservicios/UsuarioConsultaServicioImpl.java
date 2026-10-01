package com.bodega.plataforma.identidad.aplicacion.interno.consultaservicios;

import com.bodega.plataforma.identidad.aplicacion.consultaservicios.UsuarioConsultaServicio;
import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import com.bodega.plataforma.identidad.dominio.modelo.consultas.ObtenerUsuarioPorIdConsulta;
import com.bodega.plataforma.identidad.dominio.modelo.consultas.ObtenerUsuarioPorNombreConsulta;
import com.bodega.plataforma.identidad.dominio.repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UsuarioConsultaServicioImpl implements UsuarioConsultaServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioConsultaServicioImpl(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public Optional<Usuario> manejar(ObtenerUsuarioPorIdConsulta consulta) {
        return usuarioRepositorio.buscarPorId(consulta.usuarioId());
    }

    @Override
    public Optional<Usuario> manejar(ObtenerUsuarioPorNombreConsulta consulta) {
        return usuarioRepositorio.buscarPorNombreUsuario(consulta.nombreUsuario());
    }
}
