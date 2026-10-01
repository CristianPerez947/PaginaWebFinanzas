package com.bodega.plataforma.clientes.dominio.modelo.consultas;

public record ObtenerClientesPorTiendaConsulta(Long tiendaId, boolean soloActivos) {
}
