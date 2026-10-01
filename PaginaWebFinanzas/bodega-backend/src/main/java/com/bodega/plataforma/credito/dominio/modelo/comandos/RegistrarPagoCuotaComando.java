package com.bodega.plataforma.credito.dominio.modelo.comandos;

import java.time.LocalDate;

/**
 * Registra el pago completo de una cuota. Las cuotas se pagan en orden.
 */
public record RegistrarPagoCuotaComando(Long tiendaId, Long obligacionId, int numeroCuota, LocalDate fechaPagoReal) {
    public RegistrarPagoCuotaComando {
        if (tiendaId == null || obligacionId == null) throw new IllegalArgumentException("La obligación es obligatoria");
        if (numeroCuota < 1) throw new IllegalArgumentException("El número de cuota no es válido");
    }
}
