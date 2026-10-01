package com.bodega.plataforma.clientes.interfaces.rest.recursos;

import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.Capitalizacion;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.Moneda;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.TipoTasa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "ClienteRespuesta", description = "Cliente con sus condiciones de crédito")
public record ClienteRecurso(
        Long id, String dni, String nombres, String apellidos, String telefono, String direccion, boolean activo,
        Moneda moneda, BigDecimal limiteCredito, int maxMeses, int diaCorte, String horaCorte, int diaPago,
        TipoTasa tipoTasa, BigDecimal tasaCompensatoria, Capitalizacion capitalizacion,
        TipoTasa tipoTasaMora, BigDecimal tasaMora, Capitalizacion capitalizacionMora) {
}
