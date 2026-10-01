package com.bodega.plataforma.estadocuenta.aplicacion.interno.manejadoreseventos;

import com.bodega.plataforma.credito.dominio.modelo.eventos.OperacionCreditoEvento;
import com.bodega.plataforma.estadocuenta.aplicacion.comandoservicios.EstadoCuentaComandoServicio;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reacciona a los pagos registrados en crédito: actualiza los estados de cuenta del cliente y le avisa.
 */
@Component
public class PagoRegistradoManejador {

    private final EstadoCuentaComandoServicio comandoServicio;

    public PagoRegistradoManejador(EstadoCuentaComandoServicio comandoServicio) {
        this.comandoServicio = comandoServicio;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alRegistrarOperacion(OperacionCreditoEvento evento) {
        if ("REGISTRAR_PAGO".equals(evento.accion()))
            comandoServicio.registrarPagoRecibido(evento.tiendaId(), evento.clienteId(), evento.monto());
    }
}
