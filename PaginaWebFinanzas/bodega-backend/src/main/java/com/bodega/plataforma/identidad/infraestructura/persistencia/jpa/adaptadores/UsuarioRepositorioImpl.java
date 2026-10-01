package com.bodega.plataforma.identidad.infraestructura.persistencia.jpa.adaptadores;

import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import com.bodega.plataforma.identidad.dominio.modelo.objetosvalor.Rol;
import com.bodega.plataforma.identidad.dominio.repositorios.UsuarioRepositorio;
import com.bodega.plataforma.identidad.infraestructura.persistencia.jpa.ensambladores.UsuarioPersistenciaEnsamblador;
import com.bodega.plataforma.identidad.infraestructura.persistencia.jpa.repositorios.UsuarioPersistenciaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa el puerto de repositorio de usuarios sobre Spring Data JPA.
 */
@Repository
public class UsuarioRepositorioImpl implements UsuarioRepositorio {

    private final UsuarioPersistenciaRepositorio repositorio;

    public UsuarioRepositorioImpl(UsuarioPersistenciaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repositorio.findById(id).map(UsuarioPersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return repositorio.findByNombreUsuario(nombreUsuario).map(UsuarioPersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public List<Usuario> buscarPorTienda(Long tiendaId) {
        return repositorio.findByTiendaId(tiendaId).stream()
                .map(UsuarioPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public boolean existePorNombreUsuario(String nombreUsuario) {
        return repositorio.existsByNombreUsuario(nombreUsuario);
    }

    @Override
    public boolean existePorRol(Rol rol) {
        return repositorio.existsByRol(rol);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        var guardado = repositorio.save(UsuarioPersistenciaEnsamblador.aPersistenciaDesdeDominio(usuario));
        return UsuarioPersistenciaEnsamblador.aDominioDesdePersistencia(guardado);
    }
}
