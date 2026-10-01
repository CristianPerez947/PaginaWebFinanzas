package com.bodega.plataforma.catalogo.infraestructura.persistencia.jpa.adaptadores;

import com.bodega.plataforma.catalogo.dominio.modelo.agregados.Producto;
import com.bodega.plataforma.catalogo.dominio.repositorios.ProductoRepositorio;
import com.bodega.plataforma.catalogo.infraestructura.persistencia.jpa.ensambladores.ProductoPersistenciaEnsamblador;
import com.bodega.plataforma.catalogo.infraestructura.persistencia.jpa.repositorios.ProductoPersistenciaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepositorioImpl implements ProductoRepositorio {

    private final ProductoPersistenciaRepositorio repositorio;

    public ProductoRepositorioImpl(ProductoPersistenciaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Producto> buscarPorIdYTienda(Long id, Long tiendaId) {
        return repositorio.findByIdAndTiendaId(id, tiendaId).map(ProductoPersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public List<Producto> buscarPorTienda(Long tiendaId) {
        return repositorio.findByTiendaIdOrderByCategoriaAscNombreAsc(tiendaId).stream()
                .map(ProductoPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public List<Producto> buscarActivosPorTienda(Long tiendaId) {
        return repositorio.findByTiendaIdAndActivoTrueOrderByCategoriaAscNombreAsc(tiendaId).stream()
                .map(ProductoPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public Producto guardar(Producto producto) {
        var guardado = repositorio.save(ProductoPersistenciaEnsamblador.aPersistenciaDesdeDominio(producto));
        return ProductoPersistenciaEnsamblador.aDominioDesdePersistencia(guardado);
    }
}
