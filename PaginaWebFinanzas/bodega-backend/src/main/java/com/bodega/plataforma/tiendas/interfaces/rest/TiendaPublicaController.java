package com.bodega.plataforma.tiendas.interfaces.rest;

import com.bodega.plataforma.compartido.interfaces.seguridad.UsuarioActual;
import com.bodega.plataforma.tiendas.aplicacion.consultaservicios.TiendaConsultaServicio;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTiendaPorIdConsulta;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTiendaPorSlugConsulta;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTodasLasTiendasConsulta;
import com.bodega.plataforma.tiendas.interfaces.rest.recursos.TiendaPublicaRecurso;
import com.bodega.plataforma.tiendas.interfaces.rest.transformacion.TiendaRecursoEnsamblador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Datos públicos de las tiendas (página de la bodega) y "mi tienda" para el usuario autenticado.
 */
@RestController
@RequestMapping(produces = APPLICATION_JSON_VALUE)
@Tag(name = "Tiendas públicas", description = "Consulta pública de tiendas activas")
public class TiendaPublicaController {

    private final TiendaConsultaServicio consultaServicio;

    public TiendaPublicaController(TiendaConsultaServicio consultaServicio) {
        this.consultaServicio = consultaServicio;
    }

    @GetMapping("/api/v1/publico/tiendas")
    @Operation(summary = "Lista las tiendas activas")
    public ResponseEntity<List<TiendaPublicaRecurso>> listarActivas() {
        var tiendas = consultaServicio.manejar(new ObtenerTodasLasTiendasConsulta(true)).stream()
                .map(TiendaRecursoEnsamblador::aRecursoPublicoDesdeEntidad).toList();
        return ResponseEntity.ok(tiendas);
    }

    @GetMapping("/api/v1/publico/tiendas/{slug}")
    @Operation(summary = "Obtiene una tienda activa por su identificador de URL")
    public ResponseEntity<TiendaPublicaRecurso> obtenerPorSlug(@PathVariable String slug) {
        return consultaServicio.manejar(new ObtenerTiendaPorSlugConsulta(slug))
                .filter(t -> t.isActiva())
                .map(t -> ResponseEntity.ok(TiendaRecursoEnsamblador.aRecursoPublicoDesdeEntidad(t)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/v1/mi-tienda")
    @Operation(summary = "Tienda del usuario autenticado (vendedor o cliente)")
    public ResponseEntity<TiendaPublicaRecurso> miTienda() {
        return consultaServicio.manejar(new ObtenerTiendaPorIdConsulta(UsuarioActual.tiendaId()))
                .map(t -> ResponseEntity.ok(TiendaRecursoEnsamblador.aRecursoPublicoDesdeEntidad(t)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
