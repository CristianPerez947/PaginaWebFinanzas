package com.bodega.plataforma.estadocuenta.dominio.modelo.agregados;

import com.bodega.plataforma.compartido.dominio.modelo.agregados.RaizAgregadoDominio;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoMensaje;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Raíz de agregado MensajeWhatsApp: aviso al cliente por WhatsApp. En esta versión el envío es simulado:
 * el mensaje se guarda y se muestra al vendedor como "enviado".
 */
@Getter
public class MensajeWhatsApp extends RaizAgregadoDominio<MensajeWhatsApp> {

    @Setter
    private Long id;
    @Setter
    private Long tiendaId;
    @Setter
    private Long clienteId;
    @Setter
    private String nombreCliente;
    @Setter
    private String telefono;
    @Setter
    private TipoMensaje tipo;
    /** Identificador del estado de cuenta al que se refiere el mensaje (para no repetir avisos). */
    @Setter
    private Long referenciaId;
    @Setter
    private String contenido;
    @Setter
    private LocalDateTime fechaEnvio;
    @Setter
    private String estadoEnvio = "ENVIADO (SIMULADO)";

    public MensajeWhatsApp() {
    }

    public MensajeWhatsApp(Long tiendaId, Long clienteId, String nombreCliente, String telefono, TipoMensaje tipo,
                           Long referenciaId, String contenido, LocalDateTime fechaEnvio) {
        this.tiendaId = tiendaId;
        this.clienteId = clienteId;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
    }
}
