package com.bodega.plataforma.estadocuenta.dominio.repositorios;

import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EstadoCuentaRepositorio {
    Optional<EstadoCuenta> buscarPorIdYTienda(Long id, Long tiendaId);

    Optional<EstadoCuenta> buscarPorClienteYFechaCorte(Long tiendaId, Long clienteId, LocalDate fechaCorte);

    List<EstadoCuenta> buscarPorTienda(Long tiendaId);

    List<EstadoCuenta> buscarPorCliente(Long tiendaId, Long clienteId);

    List<EstadoCuenta> buscarPendientesPorTienda(Long tiendaId);

    EstadoCuenta guardar(EstadoCuenta estadoCuenta);
}
