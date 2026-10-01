package com.bodega.plataforma.auditoria.aplicacion.consultaservicios;

import com.bodega.plataforma.auditoria.dominio.modelo.agregados.RegistroAuditoria;
import com.bodega.plataforma.auditoria.dominio.modelo.consultas.ObtenerAuditoriaConsulta;

import java.util.List;

public interface AuditoriaConsultaServicio {
    List<RegistroAuditoria> manejar(ObtenerAuditoriaConsulta consulta);
}
