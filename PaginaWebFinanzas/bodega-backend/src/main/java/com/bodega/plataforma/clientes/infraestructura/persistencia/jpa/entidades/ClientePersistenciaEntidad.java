package com.bodega.plataforma.clientes.infraestructura.persistencia.jpa.entidades;

import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.Capitalizacion;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.Moneda;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.TipoTasa;
import com.bodega.plataforma.compartido.infraestructura.persistencia.jpa.entidades.EntidadPersistenciaAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "clientes", uniqueConstraints = @UniqueConstraint(columnNames = {"tienda_id", "dni"}))
@Getter
@Setter
@NoArgsConstructor
public class ClientePersistenciaEntidad extends EntidadPersistenciaAuditable {

    @Column(name = "tienda_id", nullable = false)
    private Long tiendaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 8)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, length = 9)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Moneda moneda;

    @Column(name = "limite_credito", nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteCredito;

    @Column(name = "max_meses", nullable = false)
    private int maxMeses;

    @Column(name = "dia_corte", nullable = false)
    private int diaCorte;

    @Column(name = "hora_corte", nullable = false)
    private LocalTime horaCorte;

    @Column(name = "dia_pago", nullable = false)
    private int diaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tasa", nullable = false, length = 10)
    private TipoTasa tipoTasa;

    @Column(name = "tasa_compensatoria", nullable = false, precision = 14, scale = 7)
    private BigDecimal tasaCompensatoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "capitalizacion", length = 10)
    private Capitalizacion capitalizacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tasa_mora", nullable = false, length = 10)
    private TipoTasa tipoTasaMora;

    @Column(name = "tasa_mora", nullable = false, precision = 14, scale = 7)
    private BigDecimal tasaMora;

    @Enumerated(EnumType.STRING)
    @Column(name = "capitalizacion_mora", length = 10)
    private Capitalizacion capitalizacionMora;

    @Column(nullable = false)
    private boolean activo = true;
}
