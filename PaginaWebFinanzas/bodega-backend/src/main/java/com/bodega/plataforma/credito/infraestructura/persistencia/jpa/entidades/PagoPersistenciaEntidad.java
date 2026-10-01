package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
public class PagoPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "obligacion_id", nullable = false)
    private ObligacionPersistenciaEntidad obligacion;

    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "monto_mora", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoMora;

    @Column(name = "monto_interes", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoInteres;

    @Column(name = "monto_capital", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoCapital;
}
