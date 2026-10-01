package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.adaptadores;

import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.EstadoDeuda;
import com.bodega.plataforma.credito.dominio.repositorios.ObligacionRepositorio;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.ensambladores.ObligacionPersistenciaEnsamblador;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.repositorios.ObligacionPersistenciaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ObligacionRepositorioImpl implements ObligacionRepositorio {

    private final ObligacionPersistenciaRepositorio repositorio;

    public ObligacionRepositorioImpl(ObligacionPersistenciaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Obligacion> buscarPorIdYTienda(Long id, Long tiendaId) {
        return repositorio.findByIdAndTiendaId(id, tiendaId).map(ObligacionPersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public List<Obligacion> buscarPorCliente(Long tiendaId, Long clienteId) {
        return repositorio.findByTiendaIdAndClienteIdOrderByFechaCompraAsc(tiendaId, clienteId).stream()
                .map(ObligacionPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public List<Obligacion> buscarPendientesPorCliente(Long tiendaId, Long clienteId) {
        return repositorio.findByTiendaIdAndClienteIdAndEstadoOrderByFechaCompraAsc(tiendaId, clienteId,
                EstadoDeuda.PENDIENTE).stream().map(ObligacionPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public List<Obligacion> buscarPorTienda(Long tiendaId) {
        return repositorio.findByTiendaIdOrderByFechaCompraAsc(tiendaId).stream()
                .map(ObligacionPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public Obligacion guardar(Obligacion obligacion) {
        var guardada = repositorio.save(ObligacionPersistenciaEnsamblador.aPersistenciaDesdeDominio(obligacion));
        return ObligacionPersistenciaEnsamblador.aDominioDesdePersistencia(guardada);
    }
}
