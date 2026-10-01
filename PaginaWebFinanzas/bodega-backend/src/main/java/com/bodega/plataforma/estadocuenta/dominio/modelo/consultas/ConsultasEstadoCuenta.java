package com.bodega.plataforma.estadocuenta.dominio.modelo.consultas;

/**
 * Consultas del contexto de estados de cuenta.
 */
public final class ConsultasEstadoCuenta {

    private ConsultasEstadoCuenta() {
    }

    public record ObtenerEstadoCuentaPorIdConsulta(Long tiendaId, Long estadoCuentaId) {
    }

    /** Estados de cuenta de la tienda; con clienteId nulo se listan los de todos los clientes. */
    public record ObtenerEstadosCuentaConsulta(Long tiendaId, Long clienteId) {
    }

    public record ObtenerMensajesWhatsAppConsulta(Long tiendaId, int limite) {
        public ObtenerMensajesWhatsAppConsulta {
            if (limite < 1 || limite > 500) limite = 100;
        }
    }
}
