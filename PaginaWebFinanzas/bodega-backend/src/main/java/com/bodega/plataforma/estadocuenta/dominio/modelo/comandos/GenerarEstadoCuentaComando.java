package com.bodega.plataforma.estadocuenta.dominio.modelo.comandos;

import java.time.LocalDate;

/**
 * Genera (o regenera) el estado de cuenta de un cliente para un ciclo.
 *
 * @param fechaCorte fecha de corte del ciclo; si es nula, el último corte ya vencido
 */
public record GenerarEstadoCuentaComando(Long tiendaId, Long clienteId, LocalDate fechaCorte) {
    public GenerarEstadoCuentaComando {
        if (tiendaId == null || clienteId == null) throw new IllegalArgumentException("El cliente es obligatorio");
    }
}
