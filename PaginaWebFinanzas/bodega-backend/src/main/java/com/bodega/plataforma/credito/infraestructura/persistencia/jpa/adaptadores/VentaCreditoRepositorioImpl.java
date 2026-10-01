package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.adaptadores;

import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.repositorios.VentaCreditoRepositorio;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.ensambladores.VentaPersistenciaEnsamblador;
import com.bodega.plataforma.credito.infraestructura.persistencia.jpa.repositorios.VentaPersistenciaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VentaCreditoRepositorioImpl implements VentaCreditoRepositorio {

    private final VentaPersistenciaRepositorio repositorio;

    public VentaCreditoRepositorioImpl(VentaPersistenciaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<VentaCredito> buscarPorCliente(Long tiendaId, Long clienteId) {
        return repositorio.findByTiendaIdAndClienteIdOrderByFechaHoraDesc(tiendaId, clienteId).stream()
                .map(VentaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public List<VentaCredito> buscarPorTienda(Long tiendaId) {
        return repositorio.findByTiendaIdOrderByFechaHoraDesc(tiendaId).stream()
                .map(VentaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public VentaCredito guardar(VentaCredito venta) {
        var guardada = repositorio.save(VentaPersistenciaEnsamblador.aPersistenciaDesdeDominio(venta));
        return VentaPersistenciaEnsamblador.aDominioDesdePersistencia(guardada);
    }
}
