package com.bodega.plataforma.auditoria.dominio.modelo.agregados;

import com.bodega.plataforma.auditoria.dominio.modelo.comandos.RegistrarAuditoriaComando;
import com.bodega.plataforma.compartido.dominio.modelo.agregados.RaizAgregadoDominio;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Raíz de agregado RegistroAuditoria: constancia inmutable de una operación realizada en el sistema
 * (quién, qué, sobre qué, cuándo y con qué detalle).
 */
@Getter
public class RegistroAuditoria extends RaizAgregadoDominio<RegistroAuditoria> {

    @Setter
    private Long id;
    @Setter
    private Long tiendaId;
    @Setter
    private String usuario;
    @Setter
    private String rol;
    @Setter
    private String accion;
    @Setter
    private String entidad;
    @Setter
    private Long entidadId;
    @Setter
    private String detalle;
    @Setter
    private LocalDateTime fechaHora;

    public RegistroAuditoria() {
    }

    public RegistroAuditoria(RegistrarAuditoriaComando c, LocalDateTime fechaHora) {
        this.tiendaId = c.tiendaId();
        this.usuario = c.usuario();
        this.rol = c.rol();
        this.accion = c.accion();
        this.entidad = c.entidad();
        this.entidadId = c.entidadId();
        this.detalle = c.detalle();
        this.fechaHora = fechaHora;
    }
}
