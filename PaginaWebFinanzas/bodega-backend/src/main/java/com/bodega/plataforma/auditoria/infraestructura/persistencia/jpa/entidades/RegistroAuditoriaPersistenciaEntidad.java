package com.bodega.plataforma.auditoria.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria", indexes = @Index(name = "idx_auditoria_tienda_fecha", columnList = "tienda_id, fecha_hora"))
@Getter
@Setter
@NoArgsConstructor
public class RegistroAuditoriaPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @Column(name = "tienda_id")
    private Long tiendaId;

    @Column(nullable = false, length = 80)
    private String usuario;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false, length = 60)
    private String accion;

    @Column(length = 40)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(length = 600)
    private String detalle;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
}
