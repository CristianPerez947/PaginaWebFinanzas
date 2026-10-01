package com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoLinea;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lineas_estado_cuenta")
@Getter
@Setter
@NoArgsConstructor
public class LineaEstadoCuentaPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_cuenta_id", nullable = false)
    private EstadoCuentaPersistenciaEntidad estadoCuenta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TipoLinea tipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 400)
    private String descripcion;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(name = "incluida_en_total", nullable = false)
    private boolean incluidaEnTotal;
}
