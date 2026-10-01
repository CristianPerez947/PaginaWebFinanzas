package com.bodega.plataforma.credito.aplicacion.consultaservicios;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Situación de deuda de un cliente a la fecha del sistema.
 */
public interface DeudaClienteConsultaServicio {

    /**
     * @param capitalPendiente capital de las obligaciones pendientes (base del límite de crédito)
     * @param disponible       límite menos capital pendiente (nunca negativo)
     * @param enMora           tiene importes vencidos a la fecha de consulta
     * @param totalVencido     suma de lo vencido (importe + mora) a la fecha de consulta
     */
    record DeudaCliente(Long clienteId, String moneda, BigDecimal limiteCredito, BigDecimal capitalPendiente,
                        BigDecimal disponible, boolean enMora, BigDecimal totalVencido, LocalDate fechaConsulta,
                        List<Obligacion> obligaciones) {
    }

    Resultado<DeudaCliente, ErrorAplicacion> obtener(Long tiendaId, Long clienteId);
}
