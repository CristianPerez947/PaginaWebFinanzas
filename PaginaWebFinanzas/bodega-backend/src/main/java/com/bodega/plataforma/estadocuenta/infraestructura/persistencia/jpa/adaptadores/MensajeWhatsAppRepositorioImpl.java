package com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.adaptadores;

import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.MensajeWhatsApp;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoMensaje;
import com.bodega.plataforma.estadocuenta.dominio.repositorios.MensajeWhatsAppRepositorio;
import com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.ensambladores.EstadoCuentaPersistenciaEnsamblador;
import com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.repositorios.MensajeWhatsAppPersistenciaRepositorio;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MensajeWhatsAppRepositorioImpl implements MensajeWhatsAppRepositorio {

    private final MensajeWhatsAppPersistenciaRepositorio repositorio;

    public MensajeWhatsAppRepositorioImpl(MensajeWhatsAppPersistenciaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public MensajeWhatsApp guardar(MensajeWhatsApp mensaje) {
        var guardado = repositorio.save(EstadoCuentaPersistenciaEnsamblador.aPersistenciaDesdeDominio(mensaje));
        return EstadoCuentaPersistenciaEnsamblador.aDominioDesdePersistencia(guardado);
    }

    @Override
    public List<MensajeWhatsApp> buscarRecientesPorTienda(Long tiendaId, int limite) {
        return repositorio.findByTiendaIdOrderByFechaEnvioDescIdDesc(tiendaId, PageRequest.of(0, limite)).stream()
                .map(EstadoCuentaPersistenciaEnsamblador::aDominioDesdePersistencia).toList();
    }

    @Override
    public boolean existePorReferenciaYTipo(Long referenciaId, TipoMensaje tipo) {
        return repositorio.existsByReferenciaIdAndTipo(referenciaId, tipo);
    }
}
