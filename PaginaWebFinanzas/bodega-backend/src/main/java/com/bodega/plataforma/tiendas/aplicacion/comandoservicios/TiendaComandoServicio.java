package com.bodega.plataforma.tiendas.aplicacion.comandoservicios;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.tiendas.dominio.modelo.agregados.Tienda;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.ActualizarTiendaComando;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.CambiarEstadoTiendaComando;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.CrearTiendaComando;

public interface TiendaComandoServicio {
    Resultado<Tienda, ErrorAplicacion> manejar(CrearTiendaComando comando);

    Resultado<Tienda, ErrorAplicacion> manejar(ActualizarTiendaComando comando);

    Resultado<Tienda, ErrorAplicacion> manejar(CambiarEstadoTiendaComando comando);
}
