package com.bodega.plataforma.clientes.aplicacion.comandoservicios;

import com.bodega.plataforma.clientes.dominio.modelo.agregados.Cliente;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.ActualizarClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.CambiarEstadoClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.CrearClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.RegenerarClaveClienteComando;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;

public interface ClienteComandoServicio {

    /** Cliente junto con su clave en texto plano. Se muestra una sola vez al vendedor; nunca se guarda. */
    record ClienteConClave(Cliente cliente, String claveInicial) {
    }

    Resultado<ClienteConClave, ErrorAplicacion> manejar(CrearClienteComando comando);

    Resultado<Cliente, ErrorAplicacion> manejar(ActualizarClienteComando comando);

    Resultado<Cliente, ErrorAplicacion> manejar(CambiarEstadoClienteComando comando);

    Resultado<ClienteConClave, ErrorAplicacion> manejar(RegenerarClaveClienteComando comando);
}
