package com.bodega.plataforma.identidad.dominio.repositorios;

import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio de usuarios.
 */
public interface UsuarioRepositorio {
    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);

    List<Usuario> buscarPorTienda(Long tiendaId);

    boolean existePorNombreUsuario(String nombreUsuario);

    boolean existePorRol(com.bodega.plataforma.identidad.dominio.modelo.objetosvalor.Rol rol);

    Usuario guardar(Usuario usuario);
}
