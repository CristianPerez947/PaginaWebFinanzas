package com.bodega.plataforma.auditoria.aplicacion.interno.comandoservicios;

import com.bodega.plataforma.auditoria.aplicacion.comandoservicios.AuditoriaComandoServicio;
import com.bodega.plataforma.auditoria.dominio.modelo.agregados.RegistroAuditoria;
import com.bodega.plataforma.auditoria.dominio.modelo.comandos.RegistrarAuditoriaComando;
import com.bodega.plataforma.auditoria.dominio.repositorios.AuditoriaRepositorio;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.compartido.dominio.servicios.RelojSistema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra operaciones en la auditoría con la fecha del sistema.
 */
@Service
public class AuditoriaComandoServicioImpl implements AuditoriaComandoServicio {

    private final AuditoriaRepositorio auditoriaRepositorio;
    private final RelojSistema reloj;

    public AuditoriaComandoServicioImpl(AuditoriaRepositorio auditoriaRepositorio, RelojSistema reloj) {
        this.auditoriaRepositorio = auditoriaRepositorio;
        this.reloj = reloj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Resultado<RegistroAuditoria, ErrorAplicacion> manejar(RegistrarAuditoriaComando comando) {
        try {
            return Resultado.exito(auditoriaRepositorio.guardar(new RegistroAuditoria(comando, reloj.ahora())));
        } catch (Exception e) {
            return Resultado.fallo(ErrorAplicacion.inesperado("registrar-auditoria", e.getMessage()));
        }
    }
}
