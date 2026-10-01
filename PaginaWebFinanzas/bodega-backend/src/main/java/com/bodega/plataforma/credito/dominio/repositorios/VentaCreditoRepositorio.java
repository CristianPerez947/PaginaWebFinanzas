package com.bodega.plataforma.credito.dominio.repositorios;

import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;

import java.util.List;

public interface VentaCreditoRepositorio {
    List<VentaCredito> buscarPorCliente(Long tiendaId, Long clienteId);

    List<VentaCredito> buscarPorTienda(Long tiendaId);

    VentaCredito guardar(VentaCredito venta);
}
