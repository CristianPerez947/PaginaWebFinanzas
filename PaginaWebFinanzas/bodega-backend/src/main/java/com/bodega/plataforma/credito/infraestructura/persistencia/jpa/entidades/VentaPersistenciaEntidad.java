package com.bodega.plataforma.credito.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas_credito")
@Getter
@Setter
@NoArgsConstructor
public class VentaPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @Column(name = "tienda_id", nullable = false)
    private Long tiendaId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(name = "tipo_cambio_aplicado", precision = 10, scale = 4)
    private BigDecimal tipoCambioAplicado;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<LineaVentaPersistenciaEntidad> lineas = new ArrayList<>();
}
