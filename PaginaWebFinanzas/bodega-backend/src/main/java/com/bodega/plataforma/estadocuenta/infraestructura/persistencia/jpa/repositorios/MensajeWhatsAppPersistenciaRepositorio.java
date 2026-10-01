package com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.repositorios;

import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoMensaje;
import com.bodega.plataforma.estadocuenta.infraestructura.persistencia.jpa.entidades.MensajeWhatsAppPersistenciaEntidad;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeWhatsAppPersistenciaRepositorio extends JpaRepository<MensajeWhatsAppPersistenciaEntidad, Long> {
    List<MensajeWhatsAppPersistenciaEntidad> findByTiendaIdOrderByFechaEnvioDescIdDesc(Long tiendaId, Pageable pagina);

    boolean existsByReferenciaIdAndTipo(Long referenciaId, TipoMensaje tipo);
}
