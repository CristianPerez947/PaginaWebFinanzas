package com.bodega.plataforma.analitica.interfaces.rest;

import com.bodega.plataforma.analitica.aplicacion.consultaservicios.AnaliticaConsultaServicio;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaDeudor;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaInteresMensual;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaMoroso;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaProductoVendido;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.PanelVendedor;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.ResumenCartera;
import com.bodega.plataforma.compartido.interfaces.seguridad.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Panel y reportes del vendedor. Importes en soles.
 */
@RestController
@RequestMapping(value = "/api/v1/analitica", produces = APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('VENDEDOR')")
@Tag(name = "Analítica", description = "Panel de control y reportes de la tienda")
public class AnaliticaController {

    private final AnaliticaConsultaServicio consultaServicio;

    public AnaliticaController(AnaliticaConsultaServicio consultaServicio) {
        this.consultaServicio = consultaServicio;
    }

    @GetMapping("/panel")
    @Operation(summary = "Panel principal: indicadores, gráficos y alertas")
    public ResponseEntity<PanelVendedor> panel() {
        return ResponseEntity.ok(consultaServicio.panel(UsuarioActual.tiendaId()));
    }

    @GetMapping("/reportes/deudores")
    @Operation(summary = "Reporte de deudores con su capital pendiente y lo exigible hoy")
    public ResponseEntity<List<FilaDeudor>> deudores() {
        return ResponseEntity.ok(consultaServicio.deudores(UsuarioActual.tiendaId()));
    }

    @GetMapping("/reportes/morosos")
    @Operation(summary = "Reporte de clientes morosos")
    public ResponseEntity<List<FilaMoroso>> morosos() {
        return ResponseEntity.ok(consultaServicio.morosos(UsuarioActual.tiendaId()));
    }

    @GetMapping("/reportes/ventas-por-producto")
    @Operation(summary = "Ventas acumuladas por producto")
    public ResponseEntity<List<FilaProductoVendido>> ventasPorProducto() {
        return ResponseEntity.ok(consultaServicio.ventasPorProducto(UsuarioActual.tiendaId()));
    }

    @GetMapping("/reportes/intereses")
    @Operation(summary = "Intereses compensatorios y moratorios cobrados por mes")
    public ResponseEntity<List<FilaInteresMensual>> intereses() {
        return ResponseEntity.ok(consultaServicio.intereses(UsuarioActual.tiendaId()));
    }

    @GetMapping("/reportes/cartera")
    @Operation(summary = "Cartera: capital pendiente, deuda vencida, intereses y cobros")
    public ResponseEntity<ResumenCartera> cartera() {
        return ResponseEntity.ok(consultaServicio.cartera(UsuarioActual.tiendaId()));
    }
}
