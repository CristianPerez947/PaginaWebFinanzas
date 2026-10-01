package com.bodega.plataforma.credito.dominio.modelo.eventos;

import java.math.BigDecimal;

/**
 * Evento de dominio del contexto de crédito: venta registrada, pago registrado o deuda condonada.
 *
 * @param accion REGISTRAR_VENTA, REGISTRAR_PAGO, CONDONAR_DEUDA
 * @param monto  importe asociado a la operación (total de la venta, total pagado o monto condonado)
 */
public record OperacionCreditoEvento(Long tiendaId, Long clienteId, String entidad, Long entidadId, String accion,
                                     String detalle, BigDecimal monto) {
}
