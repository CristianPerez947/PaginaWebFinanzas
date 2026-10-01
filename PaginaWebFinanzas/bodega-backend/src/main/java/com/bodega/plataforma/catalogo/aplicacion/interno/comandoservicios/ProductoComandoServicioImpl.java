package com.bodega.plataforma.catalogo.aplicacion.interno.comandoservicios;

import com.bodega.plataforma.catalogo.aplicacion.comandoservicios.ProductoComandoServicio;
import com.bodega.plataforma.catalogo.dominio.modelo.agregados.Producto;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.ActualizarProductoComando;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.CambiarEstadoProductoComando;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.CrearProductoComando;
import com.bodega.plataforma.catalogo.dominio.modelo.eventos.ProductoCambiadoEvento;
import com.bodega.plataforma.catalogo.dominio.repositorios.ProductoRepositorio;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoComandoServicioImpl implements ProductoComandoServicio {

    private final ProductoRepositorio productoRepositorio;
    private final ApplicationEventPublisher publicadorEventos;

    public ProductoComandoServicioImpl(ProductoRepositorio productoRepositorio,
                                       ApplicationEventPublisher publicadorEventos) {
        this.productoRepositorio = productoRepositorio;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public Resultado<Producto, ErrorAplicacion> manejar(CrearProductoComando comando) {
        var producto = productoRepositorio.guardar(new Producto(comando));
        publicar(producto, "CREAR_PRODUCTO", "Producto '%s' creado".formatted(producto.getNombre()));
        return Resultado.exito(producto);
    }

    @Override
    public Resultado<Producto, ErrorAplicacion> manejar(ActualizarProductoComando c) {
        var producto = productoRepositorio.buscarPorIdYTienda(c.productoId(), c.tiendaId());
        if (producto.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Producto", c.productoId().toString()));
        var actualizado = productoRepositorio.guardar(producto.get().actualizarInformacion(c.nombre(), c.categoria(),
                c.marca(), c.proveedor(), c.descripcion(), c.unidadMedida(), c.imagen(), c.precioContado(),
                c.precioCredito(), c.permiteCuotas()));
        publicar(actualizado, "ACTUALIZAR_PRODUCTO", "Producto '%s' actualizado".formatted(actualizado.getNombre()));
        return Resultado.exito(actualizado);
    }

    @Override
    public Resultado<Producto, ErrorAplicacion> manejar(CambiarEstadoProductoComando c) {
        var producto = productoRepositorio.buscarPorIdYTienda(c.productoId(), c.tiendaId());
        if (producto.isEmpty())
            return Resultado.fallo(ErrorAplicacion.noEncontrado("Producto", c.productoId().toString()));
        producto.get().cambiarEstado(c.activo());
        var guardado = productoRepositorio.guardar(producto.get());
        publicar(guardado, c.activo() ? "REACTIVAR_PRODUCTO" : "DAR_DE_BAJA_PRODUCTO",
                "Producto '%s' %s".formatted(guardado.getNombre(), c.activo() ? "reactivado" : "dado de baja"));
        return Resultado.exito(guardado);
    }

    private void publicar(Producto producto, String accion, String detalle) {
        publicadorEventos.publishEvent(new ProductoCambiadoEvento(producto.getTiendaId(), producto.getId(), accion, detalle));
    }
}
