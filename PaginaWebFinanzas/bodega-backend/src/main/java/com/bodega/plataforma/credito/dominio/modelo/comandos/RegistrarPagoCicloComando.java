package com.bodega.plataforma.credito.dominio.modelo.comandos;

import java.time.LocalDate;

/**
 * Registra el pago total de todas las obligaciones a fin de mes de un cliente cuya fecha de pago es la indicada.
 *
 * @param fechaPagoCiclo fecha de pago pactada del ciclo (identifica el estado de cuenta)
 * @param fechaPagoReal  fecha en que el cliente pagó; nula = fecha actual del sistema
 */
public record RegistrarPagoCicloComando(Long tiendaId, Long clienteId, LocalDate fechaPagoCiclo,
                                        LocalDate fechaPagoReal) {
    public RegistrarPagoCicloComando {
        if (tiendaId == null || clienteId == null) throw new IllegalArgumentException("El cliente es obligatorio");
        if (fechaPagoCiclo == null) throw new IllegalArgumentException("La fecha de pago del ciclo es obligatoria");
    }
}
