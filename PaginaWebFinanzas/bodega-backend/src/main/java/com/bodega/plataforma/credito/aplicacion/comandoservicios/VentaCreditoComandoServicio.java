package com.bodega.plataforma.credito.aplicacion.comandoservicios;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.agregados.VentaCredito;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarVentaComando;

import java.util.List;

public interface VentaCreditoComandoServicio {

    /** Venta con las obligaciones (plan de pagos) que genera. */
    record VentaConstruida(VentaCredito venta, List<Obligacion> obligaciones) {
    }

    /** Valida y guarda la venta con sus obligaciones. */
    Resultado<VentaConstruida, ErrorAplicacion> manejar(RegistrarVentaComando comando);

    /** Calcula la venta y su plan de pagos sin guardar nada (vista previa para el vendedor). */
    Resultado<VentaConstruida, ErrorAplicacion> previsualizar(RegistrarVentaComando comando);
}
