package com.bodega.plataforma.catalogo.aplicacion.comandoservicios;

import com.bodega.plataforma.catalogo.dominio.modelo.agregados.Producto;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.ActualizarProductoComando;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.CambiarEstadoProductoComando;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.CrearProductoComando;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;

public interface ProductoComandoServicio {
    Resultado<Producto, ErrorAplicacion> manejar(CrearProductoComando comando);

    Resultado<Producto, ErrorAplicacion> manejar(ActualizarProductoComando comando);

    Resultado<Producto, ErrorAplicacion> manejar(CambiarEstadoProductoComando comando);
}
