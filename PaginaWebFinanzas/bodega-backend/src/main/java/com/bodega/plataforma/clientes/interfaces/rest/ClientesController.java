package com.bodega.plataforma.clientes.interfaces.rest;

import com.bodega.plataforma.clientes.aplicacion.comandoservicios.ClienteComandoServicio;
import com.bodega.plataforma.clientes.aplicacion.consultaservicios.ClienteConsultaServicio;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.CambiarEstadoClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.RegenerarClaveClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientePorIdConsulta;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientePorUsuarioIdConsulta;
import com.bodega.plataforma.clientes.dominio.modelo.consultas.ObtenerClientesPorTiendaConsulta;
import com.bodega.plataforma.clientes.interfaces.rest.recursos.CambiarEstadoClienteRecurso;
import com.bodega.plataforma.clientes.interfaces.rest.recursos.ClienteRecurso;
import com.bodega.plataforma.clientes.interfaces.rest.recursos.GuardarClienteRecurso;
import com.bodega.plataforma.clientes.interfaces.rest.transformacion.ClienteRecursoEnsamblador;
import com.bodega.plataforma.compartido.interfaces.rest.transformacion.RespuestaEnsamblador;
import com.bodega.plataforma.compartido.interfaces.seguridad.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Gestión de clientes de la tienda (vendedor) y consulta del propio perfil (cliente).
 */
@RestController
@RequestMapping(produces = APPLICATION_JSON_VALUE)
@Tag(name = "Clientes", description = "Clientes con crédito y sus condiciones pactadas")
public class ClientesController {

    private final ClienteComandoServicio comandoServicio;
    private final ClienteConsultaServicio consultaServicio;

    public ClientesController(ClienteComandoServicio comandoServicio, ClienteConsultaServicio consultaServicio) {
        this.comandoServicio = comandoServicio;
        this.consultaServicio = consultaServicio;
    }

    @PostMapping("/api/v1/clientes")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Da de alta un cliente; devuelve su clave inicial (visible una sola vez)")
    public ResponseEntity<?> crear(@RequestBody GuardarClienteRecurso recurso) {
        var comando = ClienteRecursoEnsamblador.aComandoCrearDesdeRecurso(UsuarioActual.tiendaId(), recurso);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                ClienteRecursoEnsamblador::aRecursoConClaveDesdeEntidad, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/clientes")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Lista los clientes de la tienda (activos y dados de baja)")
    public ResponseEntity<List<ClienteRecurso>> listar() {
        var clientes = consultaServicio.manejar(new ObtenerClientesPorTiendaConsulta(UsuarioActual.tiendaId(), false))
                .stream().map(ClienteRecursoEnsamblador::aRecursoDesdeEntidad).toList();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/api/v1/clientes/{clienteId}")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Obtiene un cliente de la tienda")
    public ResponseEntity<ClienteRecurso> obtener(@PathVariable Long clienteId) {
        return consultaServicio.manejar(new ObtenerClientePorIdConsulta(UsuarioActual.tiendaId(), clienteId))
                .map(c -> ResponseEntity.ok(ClienteRecursoEnsamblador.aRecursoDesdeEntidad(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/api/v1/clientes/{clienteId}")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Actualiza los datos y condiciones de un cliente")
    public ResponseEntity<?> actualizar(@PathVariable Long clienteId, @RequestBody GuardarClienteRecurso recurso) {
        var comando = ClienteRecursoEnsamblador.aComandoActualizarDesdeRecurso(UsuarioActual.tiendaId(), clienteId, recurso);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                ClienteRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.OK);
    }

    @PatchMapping("/api/v1/clientes/{clienteId}/estado")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Da de baja o reactiva un cliente (baja lógica)")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long clienteId, @RequestBody CambiarEstadoClienteRecurso recurso) {
        var comando = new CambiarEstadoClienteComando(UsuarioActual.tiendaId(), clienteId, recurso.activo());
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                ClienteRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.OK);
    }

    @PostMapping("/api/v1/clientes/{clienteId}/regenerar-clave")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Genera una nueva clave para el cliente (visible una sola vez)")
    public ResponseEntity<?> regenerarClave(@PathVariable Long clienteId) {
        var comando = new RegenerarClaveClienteComando(UsuarioActual.tiendaId(), clienteId);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                ClienteRecursoEnsamblador::aRecursoConClaveDesdeEntidad, HttpStatus.OK);
    }

    @GetMapping("/api/v1/mi-cuenta/perfil")
    @PreAuthorize("hasAuthority('CLIENTE')")
    @Operation(summary = "Perfil y condiciones de crédito del cliente autenticado")
    public ResponseEntity<ClienteRecurso> miPerfil() {
        return consultaServicio.manejar(new ObtenerClientePorUsuarioIdConsulta(UsuarioActual.obtener().usuarioId()))
                .map(c -> ResponseEntity.ok(ClienteRecursoEnsamblador.aRecursoDesdeEntidad(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
