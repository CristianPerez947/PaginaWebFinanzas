package com.bodega.plataforma.credito.dominio.repositorios;

import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;

import java.util.List;
import java.util.Optional;

public interface ObligacionRepositorio {
    Optional<Obligacion> buscarPorIdYTienda(Long id, Long tiendaId);

    List<Obligacion> buscarPorCliente(Long tiendaId, Long clienteId);

    List<Obligacion> buscarPendientesPorCliente(Long tiendaId, Long clienteId);

    List<Obligacion> buscarPorTienda(Long tiendaId);

    Obligacion guardar(Obligacion obligacion);
}
