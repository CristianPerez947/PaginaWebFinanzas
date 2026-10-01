package com.bodega.plataforma.credito.interfaces.rest;

import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada;
import com.bodega.plataforma.compartido.dominio.servicios.RelojSistema;
import com.bodega.plataforma.compartido.interfaces.rest.transformacion.RespuestaEnsamblador;
import com.bodega.plataforma.compartido.interfaces.seguridad.UsuarioActual;
import com.bodega.plataforma.credito.aplicacion.consultaservicios.DeudaClienteConsultaServicio;
import com.bodega.plataforma.credito.aplicacion.consultaservicios.ObligacionConsultaServicio;
import com.bodega.plataforma.credito.dominio.modelo.consultas.ObtenerVentasPorClienteConsulta;
import com.bodega.plataforma.credito.interfaces.rest.recursos.CreditoRecursos.VentaRecurso;
import com.bodega.plataforma.credito.interfaces.rest.transformacion.CreditoRecursoEnsamblador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Consulta del cliente sobre su propia cuenta: deuda actual e historial de compras. Solo lectura.
 */
@RestController
@RequestMapping(value = "/api/v1/mi-cuenta", produces = APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('CLIENTE')")
@Tag(name = "Mi cuenta", description = "Consulta del cliente: deuda e historial")
public class MiCuentaController {

    private final ClientesContextoFachada clientesFachada;
    private final DeudaClienteConsultaServicio deudaServicio;
    private final ObligacionConsultaServicio obligacionServicio;
    private final RelojSistema reloj;

    public MiCuentaController(ClientesContextoFachada clientesFachada, DeudaClienteConsultaServicio deudaServicio,
                              ObligacionConsultaServicio obligacionServicio, RelojSistema reloj) {
        this.clientesFachada = clientesFachada;
        this.deudaServicio = deudaServicio;
        this.obligacionServicio = obligacionServicio;
        this.reloj = reloj;
    }

    @GetMapping("/deuda")
    @Operation(summary = "Deuda actual del cliente autenticado")
    public ResponseEntity<?> miDeuda() {
        var cliente = clientesFachada.obtenerClientePorUsuario(UsuarioActual.obtener().usuarioId());
        if (cliente.isEmpty()) return ResponseEntity.notFound().build();
        return RespuestaEnsamblador.aRespuestaDesdeResultado(
                deudaServicio.obtener(cliente.get().tiendaId(), cliente.get().id()),
                CreditoRecursoEnsamblador::aRecurso, HttpStatus.OK);
    }

    @GetMapping("/compras")
    @Operation(summary = "Historial de compras fiadas del cliente autenticado")
    public ResponseEntity<List<VentaRecurso>> misCompras() {
        var cliente = clientesFachada.obtenerClientePorUsuario(UsuarioActual.obtener().usuarioId());
        if (cliente.isEmpty()) return ResponseEntity.notFound().build();
        var ventas = obligacionServicio.manejar(new ObtenerVentasPorClienteConsulta(cliente.get().tiendaId(),
                cliente.get().id()));
        return ResponseEntity.ok(ventas.stream()
                .map(v -> CreditoRecursoEnsamblador.aRecurso(v, null, reloj.hoy())).toList());
    }
}
