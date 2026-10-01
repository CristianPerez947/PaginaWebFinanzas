package com.bodega.plataforma.auditoria.aplicacion.interno.consultaservicios;

import com.bodega.plataforma.auditoria.aplicacion.consultaservicios.AuditoriaConsultaServicio;
import com.bodega.plataforma.auditoria.dominio.modelo.agregados.RegistroAuditoria;
import com.bodega.plataforma.auditoria.dominio.modelo.consultas.ObtenerAuditoriaConsulta;
import com.bodega.plataforma.auditoria.dominio.repositorios.AuditoriaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditoriaConsultaServicioImpl implements AuditoriaConsultaServicio {

    private final AuditoriaRepositorio auditoriaRepositorio;

    public AuditoriaConsultaServicioImpl(AuditoriaRepositorio auditoriaRepositorio) {
        this.auditoriaRepositorio = auditoriaRepositorio;
    }

    @Override
    public List<RegistroAuditoria> manejar(ObtenerAuditoriaConsulta consulta) {
        return consulta.tiendaId() == null
                ? auditoriaRepositorio.buscarRecientes(consulta.limite())
                : auditoriaRepositorio.buscarRecientesPorTienda(consulta.tiendaId(), consulta.limite());
    }
}
