package com.bodega.plataforma.tiendas.dominio.modelo.consultas;

/**
 * Consulta de todas las tiendas; con soloActivas se excluyen las dadas de baja.
 */
public record ObtenerTodasLasTiendasConsulta(boolean soloActivas) {
}
