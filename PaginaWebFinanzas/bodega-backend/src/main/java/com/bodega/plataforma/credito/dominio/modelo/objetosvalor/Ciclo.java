package com.bodega.plataforma.credito.dominio.modelo.objetosvalor;

import java.time.LocalDate;

/**
 * Ciclo de crédito: fecha de corte y fecha de pago pactada a la que pertenece una compra.
 */
public record Ciclo(LocalDate fechaCorte, LocalDate fechaPago) {
}
