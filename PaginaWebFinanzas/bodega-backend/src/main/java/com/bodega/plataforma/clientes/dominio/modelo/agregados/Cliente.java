package com.bodega.plataforma.clientes.dominio.modelo.agregados;

import com.bodega.plataforma.clientes.dominio.modelo.comandos.CrearClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.CondicionesCredito;
import com.bodega.plataforma.compartido.dominio.modelo.agregados.RaizAgregadoDominio;
import lombok.Getter;
import lombok.Setter;

/**
 * Raíz de agregado Cliente: vecino que recibe crédito de la tienda, con sus condiciones pactadas.
 * La baja es lógica.
 */
@Getter
public class Cliente extends RaizAgregadoDominio<Cliente> {

    @Setter
    private Long id;
    @Setter
    private Long tiendaId;
    @Setter
    private Long usuarioId;
    @Setter
    private String dni;
    @Setter
    private String nombres;
    @Setter
    private String apellidos;
    @Setter
    private String telefono;
    @Setter
    private String direccion;
    @Setter
    private CondicionesCredito condiciones;
    @Setter
    private boolean activo = true;

    public Cliente() {
    }

    public Cliente(CrearClienteComando c) {
        this.tiendaId = c.tiendaId();
        this.dni = c.dni();
        this.nombres = c.nombres();
        this.apellidos = c.apellidos();
        this.telefono = c.telefono();
        this.direccion = c.direccion();
        this.condiciones = c.condiciones();
        this.activo = true;
    }

    public String nombreCompleto() {
        return (nombres + " " + apellidos).trim();
    }

    public Cliente actualizarInformacion(String nombres, String apellidos, String telefono, String direccion,
                                         CondicionesCredito condiciones) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.direccion = direccion;
        this.condiciones = condiciones;
        return this;
    }

    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }
}
