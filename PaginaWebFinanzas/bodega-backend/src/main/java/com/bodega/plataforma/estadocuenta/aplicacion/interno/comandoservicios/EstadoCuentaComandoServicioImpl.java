package com.bodega.plataforma.estadocuenta.aplicacion.interno.comandoservicios;

import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada;
import com.bodega.plataforma.clientes.interfaces.acl.ClientesContextoFachada.ClienteParaCredito;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.compartido.dominio.servicios.RelojSistema;
import com.bodega.plataforma.credito.interfaces.acl.CreditoContextoFachada;
import com.bodega.plataforma.estadocuenta.aplicacion.comandoservicios.EstadoCuentaComandoServicio;
import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.MensajeWhatsApp;
import com.bodega.plataforma.estadocuenta.dominio.modelo.comandos.GenerarEstadoCuentaComando;
import com.bodega.plataforma.estadocuenta.dominio.modelo.eventos.EstadoCuentaGeneradoEvento;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoMensaje;
import com.bodega.plataforma.estadocuenta.dominio.repositorios.EstadoCuentaRepositorio;
import com.bodega.plataforma.estadocuenta.dominio.repositorios.MensajeWhatsAppRepositorio;
import com.bodega.plataforma.estadocuenta.dominio.servicios.GeneradorEstadoCuenta;
import com.bodega.plataforma.tiendas.interfaces.acl.TiendasContextoFachada;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio de aplicación que genera los estados de cuenta al llegar la fecha de corte de cada cliente y
 * los mantiene al día (mora), además de los avisos por WhatsApp simulado.
 */
@Service
@Transactional
public class EstadoCuentaComandoServicioImpl implements EstadoCuentaComandoServicio {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClientesContextoFachada clientesFachada;
    private final CreditoContextoFachada creditoFachada;
    private final TiendasContextoFachada tiendasFachada;
    private final EstadoCuentaRepositorio estadoCuentaRepositorio;
    private final MensajeWhatsAppRepositorio mensajeRepositorio;
    private final RelojSistema reloj;
    private final ApplicationEventPublisher publicadorEventos;

    public EstadoCuentaComandoServicioImpl(ClientesContextoFachada clientesFachada, CreditoContextoFachada creditoFachada,
                                           TiendasContextoFachada tiendasFachada,
                                           EstadoCuentaRepositorio estadoCuentaRepositorio,
                                           MensajeWhatsAppRepositorio mensajeRepositorio, RelojSistema reloj,
                                           ApplicationEventPublisher publicadorEventos) {
        this.clientesFachada = clientesFachada;
        this.creditoFachada = creditoFachada;
        this.tiendasFachada = tiendasFachada;
        this.estadoCuentaRepositorio = estadoCuentaRepositorio;
        this.mensajeRepositorio = mensajeRepositorio;
        this.reloj = reloj;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public Resultado<EstadoCuenta, ErrorAplicacion> manejar(GenerarEstadoCuentaComando comando) {
        var clienteOpt = clientesFachada.obtenerCliente(comando.tiendaId(), comando.clienteId());
        if (clienteOpt.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Cliente", comando.clienteId().toString()));
        var cliente = clienteOpt.get();
        var ahora = reloj.ahora();
        var fechaCorte = comando.fechaCorte() != null ? comando.fechaCorte() : ultimoCorteVencido(cliente, ahora);
        var fechaPago = fechaPagoDelCorte(cliente, fechaCorte);

        var lineas = GeneradorEstadoCuenta.generarLineas(fechaCorte, fechaPago,
                creditoFachada.obligacionesDeCliente(comando.tiendaId(), comando.clienteId()),
                creditoFachada.ventasDeCliente(comando.tiendaId(), comando.clienteId()), ahora.toLocalDate());
        var existente = estadoCuentaRepositorio.buscarPorClienteYFechaCorte(comando.tiendaId(), comando.clienteId(), fechaCorte);
        if (lineas.isEmpty() && existente.isEmpty())
            return Resultado.fallo(ErrorAplicacion.reglaDeNegocio("sin movimientos",
                    "El cliente no tiene compras ni cuotas en el ciclo con corte al %s".formatted(fechaCorte.format(FECHA))));

        var estado = existente.orElseGet(() -> new EstadoCuenta(comando.tiendaId(), comando.clienteId(),
                cliente.moneda(), fechaCorte, fechaPago));
        var hayPendiente = lineas.stream().anyMatch(l -> l.isIncluidaEnTotal());
        if (existente.isPresent() && !hayPendiente) {
            estado.marcarPagado();
        } else {
            estado.reemplazarLineas(lineas, ahora);
            if (!hayPendiente) estado.marcarPagado();
        }
        var guardado = estadoCuentaRepositorio.guardar(estado);

        if (existente.isEmpty() && guardado.estaPendiente()) enviarEstadoCuenta(cliente, guardado, ahora);
        publicadorEventos.publishEvent(new EstadoCuentaGeneradoEvento(comando.tiendaId(), comando.clienteId(),
                guardado.getId(), "Estado de cuenta del corte %s de %s: total a pagar %s %s (%s)".formatted(
                fechaCorte.format(FECHA), cliente.nombreCompleto(), guardado.getMoneda(), guardado.getTotalAPagar(),
                existente.isPresent() ? "regenerado" : "generado")));
        return Resultado.exito(guardado);
    }

    @Override
    public void refrescarPendientes(Long tiendaId) {
        var ahora = reloj.ahora();
        for (var estado : estadoCuentaRepositorio.buscarPendientesPorTienda(tiendaId)) {
            var cliente = clientesFachada.obtenerCliente(tiendaId, estado.getClienteId());
            if (cliente.isEmpty()) continue;
            var lineas = GeneradorEstadoCuenta.generarLineas(estado.getFechaCorte(), estado.getFechaPago(),
                    creditoFachada.obligacionesDeCliente(tiendaId, estado.getClienteId()),
                    creditoFachada.ventasDeCliente(tiendaId, estado.getClienteId()), ahora.toLocalDate());
            if (lineas.stream().noneMatch(l -> l.isIncluidaEnTotal())) estado.marcarPagado();
            else estado.reemplazarLineas(lineas, ahora);
            var guardado = estadoCuentaRepositorio.guardar(estado);
            if (guardado.estaPendiente() && guardado.getFechaPago().equals(ahora.toLocalDate())
                    && !mensajeRepositorio.existePorReferenciaYTipo(guardado.getId(), TipoMensaje.RECORDATORIO_VENCIMIENTO))
                enviarRecordatorio(cliente.get(), guardado, ahora);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarPagoRecibido(Long tiendaId, Long clienteId, BigDecimal monto) {
        var clienteOpt = clientesFachada.obtenerCliente(tiendaId, clienteId);
        if (clienteOpt.isEmpty()) return;
        var cliente = clienteOpt.get();
        var ahora = reloj.ahora();
        for (var estado : estadoCuentaRepositorio.buscarPorCliente(tiendaId, clienteId)) {
            if (!estado.estaPendiente()) continue;
            var lineas = GeneradorEstadoCuenta.generarLineas(estado.getFechaCorte(), estado.getFechaPago(),
                    creditoFachada.obligacionesDeCliente(tiendaId, clienteId),
                    creditoFachada.ventasDeCliente(tiendaId, clienteId), ahora.toLocalDate());
            if (lineas.stream().noneMatch(l -> l.isIncluidaEnTotal())) estado.marcarPagado();
            else estado.reemplazarLineas(lineas, ahora);
            estadoCuentaRepositorio.guardar(estado);
        }
        var tienda = tiendasFachada.obtenerNombre(tiendaId).orElse("la tienda");
        mensajeRepositorio.guardar(new MensajeWhatsApp(tiendaId, clienteId, cliente.nombreCompleto(), cliente.telefono(),
                TipoMensaje.PAGO_RECIBIDO, null,
                "Hola %s, en %s recibimos tu pago de %s %s. ¡Gracias por tu puntualidad!".formatted(
                        primerNombre(cliente), tienda, cliente.moneda(), monto), ahora));
    }

    private void enviarEstadoCuenta(ClienteParaCredito cliente, EstadoCuenta estado, LocalDateTime ahora) {
        var tienda = tiendasFachada.obtenerNombre(estado.getTiendaId()).orElse("la tienda");
        mensajeRepositorio.guardar(new MensajeWhatsApp(estado.getTiendaId(), cliente.id(), cliente.nombreCompleto(),
                cliente.telefono(), TipoMensaje.ESTADO_CUENTA, estado.getId(),
                "Hola %s, tu estado de cuenta de %s con corte al %s ya está listo. Total a pagar: %s %s hasta el %s."
                        .formatted(primerNombre(cliente), tienda, estado.getFechaCorte().format(FECHA), estado.getMoneda(),
                                estado.getTotalAPagar(), estado.getFechaPago().format(FECHA)), ahora));
    }

    private void enviarRecordatorio(ClienteParaCredito cliente, EstadoCuenta estado, LocalDateTime ahora) {
        var tienda = tiendasFachada.obtenerNombre(estado.getTiendaId()).orElse("la tienda");
        mensajeRepositorio.guardar(new MensajeWhatsApp(estado.getTiendaId(), cliente.id(), cliente.nombreCompleto(),
                cliente.telefono(), TipoMensaje.RECORDATORIO_VENCIMIENTO, estado.getId(),
                "Hola %s, hoy vence tu pago de %s %s en %s. Pasada la fecha se aplican intereses por mora."
                        .formatted(primerNombre(cliente), estado.getMoneda(), estado.getTotalAPagar(), tienda), ahora));
    }

    private static String primerNombre(ClienteParaCredito cliente) {
        var partes = cliente.nombreCompleto().split(" ");
        return partes.length > 0 ? partes[0] : cliente.nombreCompleto();
    }

    /** Último corte del cliente cuya fecha y hora ya pasaron. */
    static LocalDate ultimoCorteVencido(ClienteParaCredito cliente, LocalDateTime ahora) {
        var esteMes = ahora.toLocalDate().withDayOfMonth(cliente.diaCorte());
        return esteMes.atTime(cliente.horaCorte()).isAfter(ahora) ? esteMes.minusMonths(1) : esteMes;
    }

    static LocalDate fechaPagoDelCorte(ClienteParaCredito cliente, LocalDate fechaCorte) {
        return cliente.diaPago() > cliente.diaCorte()
                ? fechaCorte.withDayOfMonth(cliente.diaPago())
                : fechaCorte.plusMonths(1).withDayOfMonth(cliente.diaPago());
    }
}
