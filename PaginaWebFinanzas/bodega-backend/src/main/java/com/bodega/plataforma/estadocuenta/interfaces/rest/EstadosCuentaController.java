package com.bodega.plataforma.estadocuenta.interfaces.rest;

import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.compartido.interfaces.rest.transformacion.ErrorRespuestaEnsamblador;
import com.bodega.plataforma.compartido.interfaces.rest.transformacion.RespuestaEnsamblador;
import com.bodega.plataforma.compartido.interfaces.seguridad.UsuarioActual;
import com.bodega.plataforma.estadocuenta.aplicacion.comandoservicios.EstadoCuentaComandoServicio;
import com.bodega.plataforma.estadocuenta.aplicacion.consultaservicios.EstadoCuentaConsultaServicio;
import com.bodega.plataforma.estadocuenta.aplicacion.interno.tareas.CorteAutomaticoTarea;
import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.comandos.GenerarEstadoCuentaComando;
import com.bodega.plataforma.estadocuenta.dominio.modelo.consultas.ConsultasEstadoCuenta.ObtenerEstadoCuentaPorIdConsulta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.consultas.ConsultasEstadoCuenta.ObtenerEstadosCuentaConsulta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.consultas.ConsultasEstadoCuenta.ObtenerMensajesWhatsAppConsulta;
import com.bodega.plataforma.estadocuenta.infraestructura.pdf.EstadoCuentaPdfGenerador;
import com.bodega.plataforma.estadocuenta.interfaces.rest.recursos.EstadoCuentaRecursos.CortesGeneradosRecurso;
import com.bodega.plataforma.estadocuenta.interfaces.rest.recursos.EstadoCuentaRecursos.EstadoCuentaRecurso;
import com.bodega.plataforma.estadocuenta.interfaces.rest.recursos.EstadoCuentaRecursos.GenerarEstadoCuentaSolicitud;
import com.bodega.plataforma.estadocuenta.interfaces.rest.recursos.EstadoCuentaRecursos.MensajeWhatsAppRecurso;
import com.bodega.plataforma.estadocuenta.interfaces.rest.transformacion.EstadoCuentaRecursoEnsamblador;
import com.bodega.plataforma.tiendas.interfaces.acl.TiendasContextoFachada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Estados de cuenta: los gestiona el vendedor (generar cortes, consultar, descargar PDF) y el cliente
 * consulta los suyos. También expone los mensajes de WhatsApp simulados.
 */
@RestController
@RequestMapping(produces = APPLICATION_JSON_VALUE)
@Tag(name = "Estados de cuenta", description = "Listados de pago al corte, PDF y avisos por WhatsApp (simulado)")
public class EstadosCuentaController {

    private final EstadoCuentaComandoServicio comandoServicio;
    private final EstadoCuentaConsultaServicio consultaServicio;
    private final CorteAutomaticoTarea corteTarea;
    private final EstadoCuentaPdfGenerador pdfGenerador;
    private final ClientesContextoFachada clientesFachada;
    private final TiendasContextoFachada tiendasFachada;

    public EstadosCuentaController(EstadoCuentaComandoServicio comandoServicio,
                                   EstadoCuentaConsultaServicio consultaServicio, CorteAutomaticoTarea corteTarea,
                                   EstadoCuentaPdfGenerador pdfGenerador, ClientesContextoFachada clientesFachada,
                                   TiendasContextoFachada tiendasFachada) {
        this.comandoServicio = comandoServicio;
        this.consultaServicio = consultaServicio;
        this.corteTarea = corteTarea;
        this.pdfGenerador = pdfGenerador;
        this.clientesFachada = clientesFachada;
        this.tiendasFachada = tiendasFachada;
    }

    // ---------- Vendedor ----------

    @GetMapping("/api/v1/estados-cuenta")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Estados de cuenta de la tienda, opcionalmente de un cliente")
    public ResponseEntity<List<EstadoCuentaRecurso>> listar(@RequestParam(required = false) Long clienteId) {
        var tiendaId = UsuarioActual.tiendaId();
        return ResponseEntity.ok(consultaServicio.manejar(new ObtenerEstadosCuentaConsulta(tiendaId, clienteId)).stream()
                .map(e -> aRecurso(e)).toList());
    }

    @GetMapping("/api/v1/estados-cuenta/{id}")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Detalle de un estado de cuenta")
    public ResponseEntity<EstadoCuentaRecurso> obtener(@PathVariable Long id) {
        return consultaServicio.manejar(new ObtenerEstadoCuentaPorIdConsulta(UsuarioActual.tiendaId(), id))
                .map(e -> ResponseEntity.ok(aRecurso(e))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/estados-cuenta/generar")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Genera o regenera el estado de cuenta de un cliente para un corte")
    public ResponseEntity<?> generar(@RequestBody GenerarEstadoCuentaSolicitud solicitud) {
        var comando = new GenerarEstadoCuentaComando(UsuarioActual.tiendaId(), solicitud.clienteId(), solicitud.fechaCorte());
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando), this::aRecurso,
                HttpStatus.OK);
    }

    @PostMapping("/api/v1/estados-cuenta/generar-cortes")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Ejecuta el corte ahora para todos los clientes de la tienda (igual que el proceso automático)")
    public ResponseEntity<CortesGeneradosRecurso> generarCortes() {
        return ResponseEntity.ok(new CortesGeneradosRecurso(corteTarea.procesarTienda(UsuarioActual.tiendaId())));
    }

    @GetMapping(value = "/api/v1/estados-cuenta/{id}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Descarga el estado de cuenta en PDF")
    public ResponseEntity<?> pdf(@PathVariable Long id) {
        return respuestaPdf(UsuarioActual.tiendaId(), id, null);
    }

    @GetMapping("/api/v1/mensajes-whatsapp")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Mensajes de WhatsApp enviados a los clientes (simulados)")
    public ResponseEntity<List<MensajeWhatsAppRecurso>> mensajes(@RequestParam(defaultValue = "100") int limite) {
        return ResponseEntity.ok(consultaServicio.manejar(new ObtenerMensajesWhatsAppConsulta(UsuarioActual.tiendaId(), limite))
                .stream().map(EstadoCuentaRecursoEnsamblador::aRecurso).toList());
    }

    // ---------- Cliente ----------

    @GetMapping("/api/v1/mi-cuenta/estados-cuenta")
    @PreAuthorize("hasAuthority('CLIENTE')")
    @Operation(summary = "Estados de cuenta del cliente autenticado")
    public ResponseEntity<List<EstadoCuentaRecurso>> misEstadosCuenta() {
        var cliente = clientesFachada.obtenerClientePorUsuario(UsuarioActual.obtener().usuarioId());
        if (cliente.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(consultaServicio.manejar(
                new ObtenerEstadosCuentaConsulta(cliente.get().tiendaId(), cliente.get().id())).stream()
                .map(e -> aRecurso(e)).toList());
    }

    @GetMapping(value = "/api/v1/mi-cuenta/estados-cuenta/{id}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('CLIENTE')")
    @Operation(summary = "Descarga en PDF un estado de cuenta propio")
    public ResponseEntity<?> miPdf(@PathVariable Long id) {
        var cliente = clientesFachada.obtenerClientePorUsuario(UsuarioActual.obtener().usuarioId());
        if (cliente.isEmpty()) return ResponseEntity.notFound().build();
        return respuestaPdf(cliente.get().tiendaId(), id, cliente.get().id());
    }

    // ---------- Auxiliares ----------

    private ResponseEntity<?> respuestaPdf(Long tiendaId, Long id, Long soloDelCliente) {
        var estado = consultaServicio.manejar(new ObtenerEstadoCuentaPorIdConsulta(tiendaId, id));
        if (estado.isEmpty() || (soloDelCliente != null && !soloDelCliente.equals(estado.get().getClienteId())))
            return ErrorRespuestaEnsamblador.aRespuestaDesdeError(ErrorAplicacion.noEncontrado("Estado de cuenta", id.toString()));
        var cliente = clientesFachada.obtenerCliente(tiendaId, estado.get().getClienteId());
        var bytes = pdfGenerador.generar(estado.get(), tiendasFachada.obtenerNombre(tiendaId).orElse("Tienda"),
                cliente.map(c -> c.nombreCompleto()).orElse("-"), cliente.map(c -> c.dni()).orElse("-"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"estado-de-cuenta-%d.pdf\"".formatted(id))
                .contentType(MediaType.APPLICATION_PDF).body(bytes);
    }

    private EstadoCuentaRecurso aRecurso(EstadoCuenta estado) {
        var cliente = clientesFachada.obtenerCliente(estado.getTiendaId(), estado.getClienteId());
        return EstadoCuentaRecursoEnsamblador.aRecurso(estado, cliente.map(c -> c.nombreCompleto()).orElse("-"),
                cliente.map(c -> c.dni()).orElse("-"));
    }
}
