package com.bodega.plataforma.tiendas.interfaces.rest;

import com.bodega.plataforma.compartido.interfaces.rest.transformacion.RespuestaEnsamblador;
import com.bodega.plataforma.tiendas.aplicacion.comandoservicios.TiendaComandoServicio;
import com.bodega.plataforma.tiendas.aplicacion.consultaservicios.TiendaConsultaServicio;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.CambiarEstadoTiendaComando;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTiendaPorIdConsulta;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTodasLasTiendasConsulta;
import com.bodega.plataforma.tiendas.interfaces.rest.recursos.ActualizarTiendaRecurso;
import com.bodega.plataforma.tiendas.interfaces.rest.recursos.CambiarEstadoTiendaRecurso;
import com.bodega.plataforma.tiendas.interfaces.rest.recursos.CrearTiendaRecurso;
import com.bodega.plataforma.tiendas.interfaces.rest.recursos.TiendaRecurso;
import com.bodega.plataforma.tiendas.interfaces.rest.transformacion.TiendaRecursoEnsamblador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Gestión de tiendas: alta, edición, baja y reactivación. Solo el administrador del sistema.
 */
@RestController
@RequestMapping(value = "/api/v1/tiendas", produces = APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('ADMIN_SISTEMA')")
@Tag(name = "Tiendas", description = "Administración de tiendas (administrador del sistema)")
public class TiendasController {

    private final TiendaComandoServicio comandoServicio;
    private final TiendaConsultaServicio consultaServicio;

    public TiendasController(TiendaComandoServicio comandoServicio, TiendaConsultaServicio consultaServicio) {
        this.comandoServicio = comandoServicio;
        this.consultaServicio = consultaServicio;
    }

    @PostMapping
    @Operation(summary = "Da de alta una tienda y su vendedor")
    public ResponseEntity<?> crear(@RequestBody CrearTiendaRecurso recurso) {
        var comando = TiendaRecursoEnsamblador.aComandoDesdeRecurso(recurso);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                TiendaRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lista todas las tiendas (activas y dadas de baja)")
    public ResponseEntity<List<TiendaRecurso>> listar() {
        var tiendas = consultaServicio.manejar(new ObtenerTodasLasTiendasConsulta(false)).stream()
                .map(TiendaRecursoEnsamblador::aRecursoDesdeEntidad).toList();
        return ResponseEntity.ok(tiendas);
    }

    @GetMapping("/{tiendaId}")
    @Operation(summary = "Obtiene una tienda por su identificador")
    public ResponseEntity<TiendaRecurso> obtener(@PathVariable Long tiendaId) {
        return consultaServicio.manejar(new ObtenerTiendaPorIdConsulta(tiendaId))
                .map(t -> ResponseEntity.ok(TiendaRecursoEnsamblador.aRecursoDesdeEntidad(t)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{tiendaId}")
    @Operation(summary = "Actualiza los datos de una tienda")
    public ResponseEntity<?> actualizar(@PathVariable Long tiendaId, @RequestBody ActualizarTiendaRecurso recurso) {
        var comando = TiendaRecursoEnsamblador.aComandoDesdeRecurso(tiendaId, recurso);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                TiendaRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.OK);
    }

    @PatchMapping("/{tiendaId}/estado")
    @Operation(summary = "Da de baja o reactiva una tienda (baja lógica)")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long tiendaId, @RequestBody CambiarEstadoTiendaRecurso recurso) {
        var comando = new CambiarEstadoTiendaComando(tiendaId, recurso.activa());
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                TiendaRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.OK);
    }
}
