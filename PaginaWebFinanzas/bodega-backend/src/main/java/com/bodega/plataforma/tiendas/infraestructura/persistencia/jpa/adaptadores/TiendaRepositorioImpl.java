package com.bodega.plataforma.tiendas.infraestructura.persistencia.jpa.adaptadores;

import com.bodega.plataforma.tiendas.dominio.modelo.agregados.Tienda;
import com.bodega.plataforma.tiendas.dominio.repositorios.TiendaRepositorio;
import com.bodega.plataforma.tiendas.infraestructura.persistencia.jpa.ensambladores.TiendaPersistenciaEnsamblador;
import com.bodega.plataforma.tiendas.infraestructura.persistencia.jpa.repositorios.TiendaPersistenciaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TiendaRepositorioImpl implements TiendaRepositorio {

    private final TiendaPersistenciaRepositorio repositorio;

    public TiendaRepositorioImpl(TiendaPersistenciaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Tienda> buscarPorId(Long id) {
        return repositorio.findById(id).map(TiendaPersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public Optional<Tienda> buscarPorSlug(String slug) {
        return repositorio.findBySlug(slug).map(TiendaPersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public List<Tienda> buscarTodas() {
        return repositorio.findAll().stream().map(TiendaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public List<Tienda> buscarActivas() {
        return repositorio.findByActivaTrueOrderByNombreAsc().stream()
                .map(TiendaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public boolean existePorSlug(String slug) {
        return repositorio.existsBySlug(slug);
    }

    @Override
    public Tienda guardar(Tienda tienda) {
        var guardada = repositorio.save(TiendaPersistenciaEnsamblador.aPersistenciaDesdeDominio(tienda));
        return TiendaPersistenciaEnsamblador.aDominioDesdePersistencia(guardada);
    }
}
