package com.bodega.plataforma.credito.dominio.modelo.comandos;

import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comando para registrar (o previsualizar) una venta fiada a un cliente.
 *
 * @param tiendaId  tienda del vendedor (se toma del token)
 * @param clienteId cliente que recibe el crédito
 * @param lineas    productos vendidos; cada línea elige su modalidad de pago
 */
public record RegistrarVentaComando(Long tiendaId, Long clienteId, List<LineaVentaComando> lineas) {

    /**
     * Línea de la venta.
     *
     * @param productoId producto activo de la tienda
     * @param cantidad   cantidad mayor que cero (admite decimales, por ejemplo kilos)
     * @param modalidad  CONTADO, FIN_DE_MES o CUOTAS
     * @param numCuotas  número de cuotas (2 hasta el máximo del cliente); solo para CUOTAS
     */
    public record LineaVentaComando(Long productoId, BigDecimal cantidad, Modalidad modalidad, Integer numCuotas) {
        public LineaVentaComando {
            if (productoId == null) throw new IllegalArgumentException("El producto es obligatorio");
            if (cantidad == null || cantidad.signum() <= 0)
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
            if (modalidad == null) throw new IllegalArgumentException("La modalidad de pago es obligatoria");
            if (modalidad == Modalidad.CUOTAS && (numCuotas == null || numCuotas < 2))
                throw new IllegalArgumentException("Las compras en cuotas requieren al menos 2 cuotas");
            if (modalidad != Modalidad.CUOTAS) numCuotas = null;
        }
    }

    public RegistrarVentaComando {
        if (tiendaId == null || clienteId == null) throw new IllegalArgumentException("El cliente es obligatorio");
        if (lineas == null || lineas.isEmpty()) throw new IllegalArgumentException("La venta debe tener al menos un producto");
    }
}
