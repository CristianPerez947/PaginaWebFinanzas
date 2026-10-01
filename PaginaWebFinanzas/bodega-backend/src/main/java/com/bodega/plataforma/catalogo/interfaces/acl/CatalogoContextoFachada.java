package com.bodega.plataforma.catalogo.interfaces.acl;

import com.bodega.plataforma.catalogo.aplicacion.consultaservicios.ProductoConsultaServicio;
import com.bodega.plataforma.catalogo.dominio.modelo.consultas.ObtenerProductoPorIdConsulta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Fachada ACL del catálogo: datos de un producto activo necesarios para registrar una venta.
 */
@Service
public class CatalogoContextoFachada {

    /** Datos de un producto tal como se venden (precio congelado al momento de la venta). */
    public record ProductoParaVenta(Long id, String nombre, BigDecimal precioContado, BigDecimal precioCredito,
                                    boolean permiteCuotas) {
    }

    private final ProductoConsultaServicio consultaServicio;

    public CatalogoContextoFachada(ProductoConsultaServicio consultaServicio) {
        this.consultaServicio = consultaServicio;
    }

    public Optional<ProductoParaVenta> obtenerProductoActivo(Long tiendaId, Long productoId) {
        return consultaServicio.manejar(new ObtenerProductoPorIdConsulta(tiendaId, productoId))
                .filter(p -> p.isActivo())
                .map(p -> new ProductoParaVenta(p.getId(), p.getNombre(), p.getPrecioContado(), p.getPrecioCredito(),
                        p.isPermiteCuotas()));
    }
}
