package com.bodega.plataforma.estadocuenta.dominio.repositorios;

import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.MensajeWhatsApp;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoMensaje;

import java.util.List;

public interface MensajeWhatsAppRepositorio {
    MensajeWhatsApp guardar(MensajeWhatsApp mensaje);

    List<MensajeWhatsApp> buscarRecientesPorTienda(Long tiendaId, int limite);

    boolean existePorReferenciaYTipo(Long referenciaId, TipoMensaje tipo);
}
