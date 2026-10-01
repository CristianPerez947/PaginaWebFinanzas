package com.bodega.plataforma.estadocuenta.aplicacion.interno.consultaservicios;

import com.bodega.plataforma.estadocuenta.aplicacion.consultaservicios.EstadoCuentaConsultaServicio;
import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.MensajeWhatsApp;
import com.bodega.plataforma.estadocuenta.dominio.modelo.consultas.ConsultasEstadoCuenta.ObtenerEstadoCuentaPorIdConsulta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.consultas.ConsultasEstadoCuenta.ObtenerEstadosCuentaConsulta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.consultas.ConsultasEstadoCuenta.ObtenerMensajesWhatsAppConsulta;
import com.bodega.plataforma.estadocuenta.dominio.repositorios.EstadoCuentaRepositorio;
import com.bodega.plataforma.estadocuenta.dominio.repositorios.MensajeWhatsAppRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EstadoCuentaConsultaServicioImpl implements EstadoCuentaConsultaServicio {

    private final EstadoCuentaRepositorio estadoCuentaRepositorio;
    private final MensajeWhatsAppRepositorio mensajeRepositorio;

    public EstadoCuentaConsultaServicioImpl(EstadoCuentaRepositorio estadoCuentaRepositorio,
                                            MensajeWhatsAppRepositorio mensajeRepositorio) {
        this.estadoCuentaRepositorio = estadoCuentaRepositorio;
        this.mensajeRepositorio = mensajeRepositorio;
    }

    @Override
    public Optional<EstadoCuenta> manejar(ObtenerEstadoCuentaPorIdConsulta c) {
        return estadoCuentaRepositorio.buscarPorIdYTienda(c.estadoCuentaId(), c.tiendaId());
    }

    @Override
    public List<EstadoCuenta> manejar(ObtenerEstadosCuentaConsulta c) {
        return c.clienteId() == null
                ? estadoCuentaRepositorio.buscarPorTienda(c.tiendaId())
                : estadoCuentaRepositorio.buscarPorCliente(c.tiendaId(), c.clienteId());
    }

    @Override
    public List<MensajeWhatsApp> manejar(ObtenerMensajesWhatsAppConsulta c) {
        return mensajeRepositorio.buscarRecientesPorTienda(c.tiendaId(), c.limite());
    }
}
