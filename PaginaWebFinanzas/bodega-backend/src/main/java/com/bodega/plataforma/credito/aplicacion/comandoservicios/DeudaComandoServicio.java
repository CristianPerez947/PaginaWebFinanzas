package com.bodega.plataforma.credito.aplicacion.comandoservicios;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.credito.dominio.modelo.comandos.CondonarDeudaClienteComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.CondonarObligacionComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarPagoCicloComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarPagoCuotaComando;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DeudaComandoServicio {

    /** Resumen de un pago registrado, con el desglose de la prelación (mora, interés, capital). */
    record ResumenPago(int obligaciones, LocalDate fechaPago, BigDecimal mora, BigDecimal interes,
                       BigDecimal capital, BigDecimal total) {
    }

    /** Resumen de una condonación. */
    record ResumenCondonacion(int obligaciones, BigDecimal montoCondonado) {
    }

    Resultado<ResumenPago, ErrorAplicacion> manejar(RegistrarPagoCicloComando comando);

    Resultado<ResumenPago, ErrorAplicacion> manejar(RegistrarPagoCuotaComando comando);

    Resultado<ResumenCondonacion, ErrorAplicacion> manejar(CondonarObligacionComando comando);

    Resultado<ResumenCondonacion, ErrorAplicacion> manejar(CondonarDeudaClienteComando comando);
}
