package com.bodega.plataforma.auditoria.interfaces.rest;

import com.bodega.plataforma.auditoria.aplicacion.consultaservicios.AuditoriaConsultaServicio;
import com.bodega.plataforma.auditoria.dominio.modelo.consultas.ObtenerAuditoriaConsulta;
import com.bodega.plataforma.compartido.interfaces.seguridad.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Consulta del registro de auditoría: el vendedor ve su tienda; el administrador del sistema ve todo.
 */
@RestController
@RequestMapping(value = "/api/v1/auditoria", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Auditoría", description = "Registro de todas las operaciones del sistema")
public class AuditoriaController {

    @Schema(name = "RegistroAuditoriaRespuesta")
    public record RegistroAuditoriaRecurso(Long id, LocalDateTime fechaHora, Long tiendaId, String usuario, String rol,
                                           String accion, String entidad, Long entidadId, String detalle) {
    }

    private final AuditoriaConsultaServicio consultaServicio;

    public AuditoriaController(AuditoriaConsultaServicio consultaServicio) {
        this.consultaServicio = consultaServicio;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VENDEDOR','ADMIN_SISTEMA')")
    @Operation(summary = "Operaciones registradas, de la más reciente a la más antigua")
    public ResponseEntity<List<RegistroAuditoriaRecurso>> listar(@RequestParam(defaultValue = "200") int limite) {
        var usuario = UsuarioActual.obtener();
        var tiendaId = UsuarioActual.ADMIN_SISTEMA.equals(usuario.rol()) ? null : usuario.tiendaId();
        var registros = consultaServicio.manejar(new ObtenerAuditoriaConsulta(tiendaId, limite)).stream()
                .map(r -> new RegistroAuditoriaRecurso(r.getId(), r.getFechaHora(), r.getTiendaId(), r.getUsuario(),
                        r.getRol(), r.getAccion(), r.getEntidad(), r.getEntidadId(), r.getDetalle()))
                .toList();
        return ResponseEntity.ok(registros);
    }
}
