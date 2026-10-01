package com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.adaptadores;

import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.EstadoEstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.repositorios.EstadoCuentaRepositorio;
import com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.ensambladores.EstadoCuentaPersistenciaEnsamblador;
import com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.repositorios.EstadoCuentaPersistenciaRepositorio;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class EstadoCuentaRepositorioImpl implements EstadoCuentaRepositorio {

    private final EstadoCuentaPersistenciaRepositorio repositorio;

    public EstadoCuentaRepositorioImpl(EstadoCuentaPersistenciaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<EstadoCuenta> buscarPorIdYTienda(Long id, Long tiendaId) {
        return repositorio.findByIdAndTiendaId(id, tiendaId).map(EstadoCuentaPersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public Optional<EstadoCuenta> buscarPorClienteYFechaCorte(Long tiendaId, Long clienteId, LocalDate fechaCorte) {
        return repositorio.findByTiendaIdAndClienteIdAndFechaCorte(tiendaId, clienteId, fechaCorte)
                .map(EstadoCuentaPersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public List<EstadoCuenta> buscarPorTienda(Long tiendaId) {
        return repositorio.findByTiendaIdOrderByFechaCorteDescIdDesc(tiendaId).stream()
                .map(EstadoCuentaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public List<EstadoCuenta> buscarPorCliente(Long tiendaId, Long clienteId) {
        return repositorio.findByTiendaIdAndClienteIdOrderByFechaCorteDesc(tiendaId, clienteId).stream()
                .map(EstadoCuentaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public List<EstadoCuenta> buscarPendientesPorTienda(Long tiendaId) {
        return repositorio.findByTiendaIdAndEstado(tiendaId, EstadoEstadoCuenta.PENDIENTE).stream()
                .map(EstadoCuentaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public EstadoCuenta guardar(EstadoCuenta estadoCuenta) {
        var guardado = repositorio.save(EstadoCuentaPersistenciaEnsamblador.aPersistenciaDesdeDominio(estadoCuenta));
        return EstadoCuentaPersistenciaEnsamblador.aDominioDesdePersistencia(guardado);
    }
}
