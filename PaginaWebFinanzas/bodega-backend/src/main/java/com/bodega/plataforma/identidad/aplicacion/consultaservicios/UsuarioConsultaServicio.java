package com.bodega.plataforma.identidad.aplicacion.consultaservicios;

import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import com.bodega.plataforma.identidad.dominio.modelo.consultas.ObtenerUsuarioPorIdConsulta;
import com.bodega.plataforma.identidad.dominio.modelo.consultas.ObtenerUsuarioPorNombreConsulta;

import java.util.Optional;

public interface UsuarioConsultaServicio {
    Optional<Usuario> manejar(ObtenerUsuarioPorIdConsulta consulta);

    Optional<Usuario> manejar(ObtenerUsuarioPorNombreConsulta consulta);
}
