package com.bodega.plataforma.tiendas.interfaces.acl;

import com.bodega.plataforma.tiendas.aplicacion.consultaservicios.TiendaConsultaServicio;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTiendaPorIdConsulta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Fachada ACL del contexto de tiendas: datos de solo lectura que otros contextos necesitan.
 */
@Service
public class TiendasContextoFachada {

    private final TiendaConsultaServicio consultaServicio;

    public TiendasContextoFachada(TiendaConsultaServicio consultaServicio) {
        this.consultaServicio = consultaServicio;
    }

    public Optional<BigDecimal> obtenerTipoCambioUsd(Long tiendaId) {
        return consultaServicio.manejar(new ObtenerTiendaPorIdConsulta(tiendaId))
                .map(t -> t.getTipoCambioUsd());
    }

    public boolean existeTiendaActiva(Long tiendaId) {
        return consultaServicio.manejar(new ObtenerTiendaPorIdConsulta(tiendaId))
                .map(t -> t.isActiva()).orElse(false);
    }

    public Optional<Long> obtenerTiendaActivaIdPorSlug(String slug) {
        return consultaServicio.manejar(new com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTiendaPorSlugConsulta(slug))
                .filter(t -> t.isActiva()).map(t -> t.getId());
    }

    public java.util.List<Long> listarIdsTiendasActivas() {
        return consultaServicio.manejar(new com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTodasLasTiendasConsulta(true))
                .stream().map(t -> t.getId()).toList();
    }

    public Optional<String> obtenerNombre(Long tiendaId) {
        return consultaServicio.manejar(new ObtenerTiendaPorIdConsulta(tiendaId)).map(t -> t.getNombre());
    }
}
