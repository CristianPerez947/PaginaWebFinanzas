package com.bodega.plataforma.catalogo.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class ProductoPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @Column(name = "tienda_id", nullable = false)
    private Long tiendaId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 80)
    private String categoria;

    @Column(length = 80)
    private String marca;

    @Column(length = 120)
    private String proveedor;

    @Column(length = 400)
    private String descripcion;

    @Column(name = "unidad_medida", nullable = false, length = 30)
    private String unidadMedida;

    @Column(length = 150)
    private String imagen;

    @Column(name = "precio_contado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioContado;

    @Column(name = "precio_credito", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCredito;

    @Column(name = "permite_cuotas", nullable = false)
    private boolean permiteCuotas;

    @Column(nullable = false)
    private boolean activo = true;
}
