package com.bodega.plataforma.estadocuenta.aplicacion.interno.tareas;

import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada;
import com.bodega.plataforma.estadocuenta.aplicacion.comandoservicios.EstadoCuentaComandoServicio;
import com.bodega.plataforma.estadocuenta.dominio.modelo.comandos.GenerarEstadoCuentaComando;
import com.bodega.plataforma.tiendas.interfaces.acl.TiendasContextoFachada;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Proceso automático: cada hora genera el estado de cuenta de los clientes cuyo corte ya venció y actualiza
 * los pendientes (mora al día y recordatorios). Cada cliente se procesa en su propia transacción.
 * Si el administrador simula una fecha, el proceso trabaja con esa fecha.
 */
@Slf4j
@Component
public class CorteAutomaticoTarea {

    private final TiendasContextoFachada tiendasFachada;
    private final ClientesContextoFachada clientesFachada;
    private final EstadoCuentaComandoServicio comandoServicio;

    public CorteAutomaticoTarea(TiendasContextoFachada tiendasFachada, ClientesContextoFachada clientesFachada,
                                EstadoCuentaComandoServicio comandoServicio) {
        this.tiendasFachada = tiendasFachada;
        this.clientesFachada = clientesFachada;
        this.comandoServicio = comandoServicio;
    }

    @Scheduled(cron = "0 5 * * * *", zone = "America/Lima")
    public void ejecutar() {
        for (var tiendaId : tiendasFachada.listarIdsTiendasActivas()) procesarTienda(tiendaId);
    }

    /** Procesa una tienda; también lo invoca el vendedor con el botón "Generar cortes". */
    public int procesarTienda(Long tiendaId) {
        int generados = 0;
        for (var cliente : clientesFachada.listarClientes(tiendaId, true)) {
            try {
                var resultado = comandoServicio.manejar(new GenerarEstadoCuentaComando(tiendaId, cliente.id(), null));
                if (resultado.esExito()) generados++;
            } catch (Exception e) {
                log.warn("No se pudo generar el estado de cuenta del cliente {}: {}", cliente.id(), e.getMessage());
            }
        }
        try {
            comandoServicio.refrescarPendientes(tiendaId);
        } catch (Exception e) {
            log.warn("No se pudieron refrescar los estados de cuenta de la tienda {}: {}", tiendaId, e.getMessage());
        }
        return generados;
    }
}
