package com.bodega.plataforma.auditoria.aplicacion.comandoservicios;

import com.bodega.plataforma.auditoria.dominio.modelo.agregados.RegistroAuditoria;
import com.bodega.plataforma.auditoria.dominio.modelo.comandos.RegistrarAuditoriaComando;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;

public interface AuditoriaComandoServicio {
    Resultado<RegistroAuditoria, ErrorAplicacion> manejar(RegistrarAuditoriaComando comando);
}
