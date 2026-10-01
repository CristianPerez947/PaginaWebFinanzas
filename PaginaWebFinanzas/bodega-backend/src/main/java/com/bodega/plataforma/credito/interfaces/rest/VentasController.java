package com.bodega.plataforma.credito.interfaces.rest;

import com.bodega.plataforma.compartido.dominio.servicios.RelojSistema;
import com.bodega.plataforma.compartido.interfaces.rest.transformacion.RespuestaEnsamblador;
import com.bodega.plataforma.compartido.interfaces.seguridad.UsuarioActual;
import com.bodega.plataforma.credito.aplicacion.comandoservicios.VentaCreditoComandoServicio;
import com.bodega.plataforma.credito.aplicacion.consultaservicios.ObligacionConsultaServicio;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerVentasPorClienteConsulta;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerVentasPorTiendaConsulta;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.RegistrarVentaSolicitud;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.VentaRecurso;
import com.bodega.plataforma.credito.interfaces.rest.transformacion.CreditoRecursoEnsamblador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Ventas fiadas: las registra el vendedor, con vista previa del plan de pagos antes de confirmar.
 */
@RestController
@RequestMapping(value = "/api/v1/ventas", produces = APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('VENDEDOR')")
@Tag(name = "Ventas", description = "Registro de ventas fiadas y plan de pagos")
public class VentasController {

    private final VentaCreditoComandoServicio comandoServicio;
    private final ObligacionConsultaServicio consultaServicio;
    private final RelojSistema reloj;

    public VentasController(VentaCreditoComandoServicio comandoServicio, ObligacionConsultaServicio consultaServicio,
                            RelojSistema reloj) {
        this.comandoServicio = comandoServicio;
        this.consultaServicio = consultaServicio;
        this.reloj = reloj;
    }

    @PostMapping
    @Operation(summary = "Registra una venta fiada (valida límite de crédito, mora y plazo)")
    public ResponseEntity<?> registrar(@RequestBody RegistrarVentaSolicitud solicitud) {
        var comando = CreditoRecursoEnsamblador.aComando(UsuarioActual.tiendaId(), solicitud);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                venta -> CreditoRecursoEnsamblador.aRecurso(venta, reloj.hoy()), HttpStatus.CREATED);
    }

    @PostMapping("/simular")
    @Operation(summary = "Calcula la venta y su plan de pagos sin guardar (vista previa)")
    public ResponseEntity<?> simular(@RequestBody RegistrarVentaSolicitud solicitud) {
        var comando = CreditoRecursoEnsamblador.aComando(UsuarioActual.tiendaId(), solicitud);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.previsualizar(comando),
                venta -> CreditoRecursoEnsamblador.aRecurso(venta, reloj.hoy()), HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Historial de ventas de la tienda, opcionalmente de un cliente")
    public ResponseEntity<List<VentaRecurso>> listar(@RequestParam(required = false) Long clienteId) {
        var tiendaId = UsuarioActual.tiendaId();
        var ventas = clienteId == null
                ? consultaServicio.manejar(new ObtenerVentasPorTiendaConsulta(tiendaId))
                : consultaServicio.manejar(new ObtenerVentasPorClienteConsulta(tiendaId, clienteId));
        return ResponseEntity.ok(ventas.stream()
                .map(v -> CreditoRecursoEnsamblador.aRecurso(v, null, reloj.hoy())).toList());
    }
}
