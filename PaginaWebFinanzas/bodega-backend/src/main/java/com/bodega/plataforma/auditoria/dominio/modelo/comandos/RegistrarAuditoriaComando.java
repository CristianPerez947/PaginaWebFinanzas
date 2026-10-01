package com.bodega.plataforma.auditoria.dominio.modelo.comandos;

/**
 * Comando para registrar una operación en la auditoría.
 *
 * @param tiendaId tienda afectada; nulo para operaciones globales del administrador del sistema
 * @param usuario  nombre del usuario que la realizó ("sistema" si fue un proceso automático)
 * @param rol      rol del usuario
 * @param accion   acción realizada (CREAR_CLIENTE, REGISTRAR_PAGO, ...)
 * @param entidad  tipo de entidad afectada
 * @param entidadId identificador de la entidad afectada
 * @param detalle  descripción de lo ocurrido
 */
public record RegistrarAuditoriaComando(Long tiendaId, String usuario, String rol, String accion, String entidad,
                                        Long entidadId, String detalle) {
    public RegistrarAuditoriaComando {
        if (accion == null || accion.isBlank()) throw new IllegalArgumentException("La acción es obligatoria");
        if (usuario == null || usuario.isBlank()) usuario = "sistema";
        if (rol == null || rol.isBlank()) rol = "SISTEMA";
    }
}
