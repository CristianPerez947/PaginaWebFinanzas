package com.bodega.plataforma.credito.interfaces.rest;

import com.bodega.plataforma.compartido.interfaces.rest.transformacion.RespuestaEnsamblador;
import com.bodega.plataforma.compartido.interfaces.seguridad.UsuarioActual;
import com.bodega.plataforma.credito.aplicacion.comandoservicios.DeudaComandoServicio;
import com.bodega.plataforma.credito.aplicacion.consultaservicios.DeudaClienteConsultaServicio;
import com.bodega.plataforma.credito.dominio.modelo.comandos.CondonarDeudaClienteComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.CondonarObligacionComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarPagoCicloComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarPagoCuotaComando;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.CondonacionSolicitud;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.PagoCicloSolicitud;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.PagoCuotaSolicitud;
import com.bodega.plataforma.credito.interfaces.rest.transformacion.CreditoRecursoEnsamblador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Deuda de los clientes de la tienda: consulta, registro de pagos y condonaciones (solo el vendedor).
 */
@RestController
@RequestMapping(value = "/api/v1", produces = APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('VENDEDOR')")
@Tag(name = "Deuda y cobranzas", description = "Pagos, mora y condonaciones")
public class DeudaController {

    private final DeudaComandoServicio comandoServicio;
    private final DeudaClienteConsultaServicio consultaServicio;

    public DeudaController(DeudaComandoServicio comandoServicio, DeudaClienteConsultaServicio consultaServicio) {
        this.comandoServicio = comandoServicio;
        this.consultaServicio = consultaServicio;
    }

    @GetMapping("/clientes/{clienteId}/deuda")
    @Operation(summary = "Deuda del cliente: obligaciones, cuotas, mora y crédito disponible")
    public ResponseEntity<?> deuda(@PathVariable Long clienteId) {
        return RespuestaEnsamblador.aRespuestaDesdeResultado(consultaServicio.obtener(UsuarioActual.tiendaId(), clienteId),
                CreditoRecursoEnsamblador::aRecurso, HttpStatus.OK);
    }

    @PostMapping("/clientes/{clienteId}/pagos-ciclo")
    @Operation(summary = "Registra el pago total del ciclo (obligaciones a fin de mes) de un cliente")
    public ResponseEntity<?> pagarCiclo(@PathVariable Long clienteId, @RequestBody PagoCicloSolicitud solicitud) {
        var comando = new RegistrarPagoCicloComando(UsuarioActual.tiendaId(), clienteId, solicitud.fechaPagoCiclo(),
                solicitud.fechaPago());
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando), Function.identity(),
                HttpStatus.OK);
    }

    @PostMapping("/obligaciones/{obligacionId}/cuotas/{numeroCuota}/pago")
    @Operation(summary = "Registra el pago completo de una cuota (en orden)")
    public ResponseEntity<?> pagarCuota(@PathVariable Long obligacionId, @PathVariable int numeroCuota,
                                        @RequestBody(required = false) PagoCuotaSolicitud solicitud) {
        var fecha = solicitud == null ? null : solicitud.fechaPago();
        var comando = new RegistrarPagoCuotaComando(UsuarioActual.tiendaId(), obligacionId, numeroCuota, fecha);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando), Function.identity(),
                HttpStatus.OK);
    }

    @PostMapping("/obligaciones/{obligacionId}/condonacion")
    @Operation(summary = "Condona una obligación pendiente (con motivo; no se borra el historial)")
    public ResponseEntity<?> condonarObligacion(@PathVariable Long obligacionId, @RequestBody CondonacionSolicitud solicitud) {
        var comando = new CondonarObligacionComando(UsuarioActual.tiendaId(), obligacionId, solicitud.motivo());
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando), Function.identity(),
                HttpStatus.OK);
    }

    @PostMapping("/clientes/{clienteId}/condonacion")
    @Operation(summary = "Condona toda la deuda pendiente de un cliente (con motivo)")
    public ResponseEntity<?> condonarDeudaCliente(@PathVariable Long clienteId, @RequestBody CondonacionSolicitud solicitud) {
        var comando = new CondonarDeudaClienteComando(UsuarioActual.tiendaId(), clienteId, solicitud.motivo());
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando), Function.identity(),
                HttpStatus.OK);
    }
}
