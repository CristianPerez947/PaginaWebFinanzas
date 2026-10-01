package com.bodega.plataforma.clientes.infraestructura.persistencia.jpa.adaptadores;

import com.bodega.plataforma.clientes.dominio.modelo.agregados.Cliente;
import com.bodega.plataforma.clientes.dominio.repositorios.ClienteRepositorio;
import com.bodega.plataforma.clientes.infraestructura.persistencia.jpa.ensambladores.ClientePersistenciaEnsamblador;
import com.bodega.plataforma.clientes.infraestructura.persistencia.jpa.repositorios.ClientePersistenciaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClienteRepositorioImpl implements ClienteRepositorio {

    private final ClientePersistenciaRepositorio repositorio;

    public ClienteRepositorioImpl(ClientePersistenciaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Cliente> buscarPorIdYTienda(Long id, Long tiendaId) {
        return repositorio.findByIdAndTiendaId(id, tiendaId).map(ClientePersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public Optional<Cliente> buscarPorUsuarioId(Long usuarioId) {
        return repositorio.findByUsuarioId(usuarioId).map(ClientePersistenciaEnsamblador::aDominioDesdePersistencia);
    }

    @Override
    public List<Cliente> buscarPorTienda(Long tiendaId) {
        return repositorio.findByTiendaIdOrderByApellidosAscNombresAsc(tiendaId).stream()
                .map(ClientePersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public List<Cliente> buscarActivosPorTienda(Long tiendaId) {
        return repositorio.findByTiendaIdAndActivoTrueOrderByApellidosAscNombresAsc(tiendaId).stream()
                .map(ClientePersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public boolean existePorTiendaYDni(Long tiendaId, String dni) {
        return repositorio.existsByTiendaIdAndDni(tiendaId, dni);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        var guardado = repositorio.save(ClientePersistenciaEnsamblador.aPersistenciaDesdeDominio(cliente));
        return ClientePersistenciaEnsamblador.aDominioDesdePersistencia(guardado);
    }
}
