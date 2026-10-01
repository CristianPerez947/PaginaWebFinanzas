package com.bodega.plataforma.compartido.interfaces.rest;

import com.bodega.plataforma.compartido.infraestructura.reloj.RelojSistemaImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Reloj del sistema: consulta abierta a usuarios autenticados; simulación solo para el administrador.
 */
@RestController
@RequestMapping(value = "/api/v1/reloj", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Reloj", description = "Fecha del sistema y modo demostración (fecha simulada)")
public class RelojController {

    public record RelojRecurso(LocalDateTime ahora, boolean simulado) {
    }

    public record SimularRelojRecurso(LocalDateTime fecha) {
    }

    private final RelojSistemaImpl reloj;

    public RelojController(RelojSistemaImpl reloj) {
        this.reloj = reloj;
    }

    @GetMapping
    @Operation(summary = "Fecha y hora actuales del sistema")
    public ResponseEntity<RelojRecurso> obtener() {
        return ResponseEntity.ok(new RelojRecurso(reloj.ahora(), reloj.estaSimulado()));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN_SISTEMA')")
    @Operation(summary = "Simula la fecha y hora del sistema (modo demostración)")
    public ResponseEntity<RelojRecurso> simular(@RequestBody SimularRelojRecurso recurso) {
        if (recurso.fecha() == null) throw new IllegalArgumentException("La fecha simulada es obligatoria");
        reloj.simular(recurso.fecha());
        return ResponseEntity.ok(new RelojRecurso(reloj.ahora(), true));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('ADMIN_SISTEMA')")
    @Operation(summary = "Vuelve al reloj real")
    public ResponseEntity<RelojRecurso> restaurar() {
        reloj.restaurarRelojReal();
        return ResponseEntity.ok(new RelojRecurso(reloj.ahora(), false));
    }
}
