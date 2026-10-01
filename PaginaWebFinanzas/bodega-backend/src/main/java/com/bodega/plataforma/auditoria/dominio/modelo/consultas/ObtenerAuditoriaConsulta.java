package com.bodega.plataforma.auditoria.dominio.modelo.consultas;

/**
 * Consulta de auditoría. Con tiendaId nulo se consultan todas las tiendas (administrador del sistema).
 */
public record ObtenerAuditoriaConsulta(Long tiendaId, int limite) {
    public ObtenerAuditoriaConsulta {
        if (limite < 1 || limite > 1000) limite = 200;
    }
}
