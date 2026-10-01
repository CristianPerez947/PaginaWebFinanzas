package com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoMensaje;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_whatsapp")
@Getter
@Setter
@NoArgsConstructor
public class MensajeWhatsAppPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @Column(name = "tienda_id", nullable = false)
    private Long tiendaId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "nombre_cliente", nullable = false, length = 200)
    private String nombreCliente;

    @Column(nullable = false, length = 9)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMensaje tipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false, length = 500)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "estado_envio", nullable = false, length = 40)
    private String estadoEnvio;
}
