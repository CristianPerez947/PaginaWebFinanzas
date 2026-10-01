package com.bodega.plataforma.estadocuenta.aplicacion.comandoservicios;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.comandos.GenerarEstadoCuentaComando;

import java.math.BigDecimal;

public interface EstadoCuentaComandoServicio {

    /** Genera o regenera el estado de cuenta de un cliente para un ciclo. */
    Resultado<EstadoCuenta, ErrorAplicacion> manejar(GenerarEstadoCuentaComando comando);

    /** Recalcula los estados de cuenta pendientes de la tienda (mora al día) y envía los recordatorios del día. */
    void refrescarPendientes(Long tiendaId);

    /** Tras un pago: actualiza los estados de cuenta del cliente y le avisa por WhatsApp (simulado). */
    void registrarPagoRecibido(Long tiendaId, Long clienteId, BigDecimal monto);
}
