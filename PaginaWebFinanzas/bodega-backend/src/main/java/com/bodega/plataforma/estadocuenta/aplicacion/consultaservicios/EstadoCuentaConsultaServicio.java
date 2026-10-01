package com.bodega.plataforma.estadocuenta.aplicacion.consultaservicios;

import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.MensajeWhatsApp;
import com.bodega.plataforma.estadocuenta.dominio.modelo.consultas.ConsultasEstadoCuenta.ObtenerEstadoCuentaPorIdConsulta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.consultas.ConsultasEstadoCuenta.ObtenerEstadosCuentaConsulta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.consultas.ConsultasEstadoCuenta.ObtenerMensajesWhatsAppConsulta;

import java.util.List;
import java.util.Optional;

public interface EstadoCuentaConsultaServicio {
    Optional<EstadoCuenta> manejar(ObtenerEstadoCuentaPorIdConsulta consulta);

    List<EstadoCuenta> manejar(ObtenerEstadosCuentaConsulta consulta);

    List<MensajeWhatsApp> manejar(ObtenerMensajesWhatsAppConsulta consulta);
}
