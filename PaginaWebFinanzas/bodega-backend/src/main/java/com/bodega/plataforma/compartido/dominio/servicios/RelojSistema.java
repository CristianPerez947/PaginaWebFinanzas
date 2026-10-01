package com.bodega.plataforma.compartido.dominio.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reloj del sistema. Todo el dominio debe usarlo en lugar de LocalDate.now(),
 * para permitir la fecha simulada durante la demostración.
 */
public interface RelojSistema {
    LocalDateTime ahora();

    default LocalDate hoy() {
        return ahora().toLocalDate();
    }
}
