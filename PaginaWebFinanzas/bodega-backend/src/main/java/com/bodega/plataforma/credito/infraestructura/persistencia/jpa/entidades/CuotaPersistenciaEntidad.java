package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.EstadoDeuda;
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
@Table(name = "cuotas")
@Getter
@Setter
@NoArgsConstructor
public class CuotaPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "obligacion_id", nullable = false)
    private ObligacionPersistenciaEntidad obligacion;

    @Column(nullable = false)
    private int numero;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal cuota;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal interes;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amortizacion;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoDeuda estado;

    @Column(name = "fecha_pago_real")
    private LocalDate fechaPagoReal;
}
