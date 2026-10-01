package com.bodega.plataforma.auditoria.aplicacion.interno.manejadoreseventos;

import com.bodega.plataforma.auditoria.aplicacion.comandoservicios.AuditoriaComandoServicio;
import com.bodega.plataforma.auditoria.dominio.modelo.comandos.RegistrarAuditoriaComando;
import com.bodega.plataforma.catalogo.dominio.modelo.eventos.ProductoCambiadoEvento;
import com.bodega.plataforma.clientes.dominio.modelo.eventos.ClienteCambiadoEvento;
import com.bodega.plataforma.compartido.interfaces.seguridad.PrincipalBodega;
import com.bodega.plataforma.credito.dominio.modelo.eventos.OperacionCreditoEvento;
import com.bodega.plataforma.estadocuenta.dominio.modelo.eventos.EstadoCuentaGeneradoEvento;
import com.bodega.plataforma.identidad.dominio.modelo.eventos.InicioSesionEvento;
import com.bodega.plataforma.tiendas.dominio.modelo.eventos.TiendaEstadoCambiadoEvento;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Escucha los eventos de dominio de todos los contextos y los registra en la auditoría, una vez confirmada
 * la transacción que los originó. El usuario se toma de la sesión; sin sesión (procesos automáticos) es "sistema".
 */
@Component
public class AuditoriaManejadorEventos {

    private final AuditoriaComandoServicio comandoServicio;

    public AuditoriaManejadorEventos(AuditoriaComandoServicio comandoServicio) {
        this.comandoServicio = comandoServicio;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alCambiarTienda(TiendaEstadoCambiadoEvento e) {
        registrar(e.tiendaId(), e.accion(), "TIENDA", e.tiendaId(), e.detalle());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alCambiarProducto(ProductoCambiadoEvento e) {
        registrar(e.tiendaId(), e.accion(), "PRODUCTO", e.productoId(), e.detalle());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alCambiarCliente(ClienteCambiadoEvento e) {
        registrar(e.tiendaId(), e.accion(), "CLIENTE", e.clienteId(), e.detalle());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alOperarCredito(OperacionCreditoEvento e) {
        registrar(e.tiendaId(), e.accion(), e.entidad(), e.entidadId(), e.detalle());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alGenerarEstadoCuenta(EstadoCuentaGeneradoEvento e) {
        comandoServicio.manejar(new RegistrarAuditoriaComando(e.tiendaId(), "sistema", "SISTEMA", "GENERAR_ESTADO_CUENTA",
                "ESTADO_CUENTA", e.estadoCuentaId(), e.detalle()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alIniciarSesion(InicioSesionEvento e) {
        comandoServicio.manejar(new RegistrarAuditoriaComando(e.tiendaId(), e.nombreUsuario(), e.rol(),
                "INICIAR_SESION", "USUARIO", e.usuarioId(), "Inicio de sesión de %s".formatted(e.nombreUsuario())));
    }

    private void registrar(Long tiendaId, String accion, String entidad, Long entidadId, String detalle) {
        String usuario = "sistema";
        String rol = "SISTEMA";
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof PrincipalBodega principal) {
            usuario = principal.nombreUsuario();
            rol = principal.rol();
        }
        comandoServicio.manejar(new RegistrarAuditoriaComando(tiendaId, usuario, rol, accion, entidad, entidadId, detalle));
    }
}
