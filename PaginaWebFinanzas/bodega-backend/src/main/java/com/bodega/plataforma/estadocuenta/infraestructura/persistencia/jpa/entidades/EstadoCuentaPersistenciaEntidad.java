package com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.EstadoEstadoCuenta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estados_cuenta", uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "fecha_corte"}))
@Getter
@Setter
@NoArgsConstructor
public class EstadoCuentaPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @Column(name = "tienda_id", nullable = false)
    private Long tiendaId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "total_compras", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalCompras;

    @Column(name = "total_intereses", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalIntereses;

    @Column(name = "total_cuotas", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalCuotas;

    @Column(name = "total_mora", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalMora;

    @Column(name = "total_a_pagar", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAPagar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoEstadoCuenta estado;

    @OneToMany(mappedBy = "estadoCuenta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC, id ASC")
    private List<LineaEstadoCuentaPersistenciaEntidad> lineas = new ArrayList<>();
}
