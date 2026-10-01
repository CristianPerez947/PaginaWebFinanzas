package com.bodega.plataforma.catalogo.interfaces.rest.transformacion;

import com.bodega.plataforma.catalogo.dominio.modelo.agregados.Producto;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.ActualizarProductoComando;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.CrearProductoComando;
import com.bodega.plataforma.catalogo.interfaces.rest.recursos.GuardarProductoRecurso;
import com.bodega.plataforma.catalogo.interfaces.rest.recursos.ProductoRecurso;

public final class ProductoRecursoEnsamblador {

    private ProductoRecursoEnsamblador() {
    }

    public static ProductoRecurso aRecursoDesdeEntidad(Producto p) {
        return new ProductoRecurso(p.getId(), p.getNombre(), p.getCategoria(), p.getMarca(), p.getProveedor(),
                p.getDescripcion(), p.getUnidadMedida(), p.getImagen(), p.getPrecioContado(), p.getPrecioCredito(),
                p.isPermiteCuotas(), p.isActivo());
    }

    public static CrearProductoComando aComandoCrearDesdeRecurso(Long tiendaId, GuardarProductoRecurso r) {
        return new CrearProductoComando(tiendaId, r.nombre(), r.categoria(), r.marca(), r.proveedor(), r.descripcion(),
                r.unidadMedida(), r.imagen(), r.precioContado(), r.precioCredito(), r.permiteCuotas());
    }

    public static ActualizarProductoComando aComandoActualizarDesdeRecurso(Long tiendaId, Long productoId,
                                                                           GuardarProductoRecurso r) {
        return new ActualizarProductoComando(tiendaId, productoId, r.nombre(), r.categoria(), r.marca(), r.proveedor(),
                r.descripcion(), r.unidadMedida(), r.imagen(), r.precioContado(), r.precioCredito(), r.permiteCuotas());
    }
}
