package com.bodega.plataforma.compartido.infraestructura.reloj;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Configuración global (una sola fila): reloj simulado para la demostración.
 */
@Entity
@Table(name = "configuracion_sistema")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionSistemaEntidad extends EntidadPersistenciaAuditable {

    @Column(name = "reloj_simulado_activo", nullable = false)
    private boolean relojSimuladoActivo;

    @Column(name = "fecha_simulada")
    private LocalDateTime fechaSimulada;
}
