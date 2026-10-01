package com.bodega.plataforma.tiendas.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tiendas")
@Getter
@Setter
@NoArgsConstructor
public class TiendaPersistenciaEntidad extends EntidadPersistenciaAuditable {

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(length = 120)
    private String logo;

    @Column(length = 11)
    private String ruc;

    @Column(length = 200)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(name = "tipo_cambio_usd", nullable = false, precision = 10, scale = 4)
    private BigDecimal tipoCambioUsd;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "nombre_dueno", nullable = false, length = 120)
    private String nombreDueno;

    @Column(name = "vendedor_usuario_id")
    private Long vendedorUsuarioId;

    @Column(name = "usuario_vendedor", length = 80)
    private String usuarioVendedor;
}
