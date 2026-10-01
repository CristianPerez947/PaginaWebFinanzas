package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.EstadoDeuda;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "obligaciones")
@Getter
@Setter
@NoArgsConstructor
public class ObligacionPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @Column(name = "tienda_id", nullable = false)
    private Long tiendaId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "venta_id")
    private Long ventaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Modalidad modalidad;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal capital;

    @Column(name = "interes_compensatorio", nullable = false, precision = 14, scale = 2)
    private BigDecimal interesCompensatorio;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "dias_gracia", nullable = false)
    private long diasGracia;

    @Column(name = "num_cuotas", nullable = false)
    private int numCuotas;

    @Column(name = "tea_compensatoria", nullable = false, precision = 14, scale = 9)
    private BigDecimal teaCompensatoria;

    @Column(name = "tea_mora", nullable = false, precision = 14, scale = 9)
    private BigDecimal teaMora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoDeuda estado;

    @Column(name = "motivo_condonacion", length = 300)
    private String motivoCondonacion;

    @Column(name = "fecha_condonacion")
    private LocalDate fechaCondonacion;

    @OneToMany(mappedBy = "obligacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    private List<CuotaPersistenciaEntidad> cuotas = new ArrayList<>();

    @OneToMany(mappedBy = "obligacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PagoPersistenciaEntidad> pagos = new ArrayList<>();
}
