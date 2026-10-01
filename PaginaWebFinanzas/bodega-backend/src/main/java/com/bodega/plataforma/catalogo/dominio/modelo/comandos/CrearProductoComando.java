package com.bodega.plataforma.catalogo.dominio.modelo.comandos;

import java.math.BigDecimal;

/**
 * Comando para dar de alta un producto o servicio.
 *
 * @param tiendaId      tienda propietaria (se toma del token, no del cliente)
 * @param nombre        nombre del producto, obligatorio
 * @param categoria     categoría (Abarrotes, Lácteos, ...)
 * @param marca         marca; opcional
 * @param proveedor     proveedor; opcional
 * @param descripcion   descripción; opcional
 * @param unidadMedida  unidad de medida (kg, unidad, litro, ...), obligatoria
 * @param imagen        nombre del archivo de la imagen en assets/productos; opcional
 * @param precioContado precio al contado (mayor que cero)
 * @param precioCredito precio de lista a crédito (no menor que el contado); si es nulo es igual al contado
 * @param permiteCuotas si puede venderse en cuotas mensuales; si no, solo a fin de mes
 */
public record CrearProductoComando(Long tiendaId, String nombre, String categoria, String marca, String proveedor,
                                   String descripcion, String unidadMedida, String imagen, BigDecimal precioContado,
                                   BigDecimal precioCredito, boolean permiteCuotas) {
    public CrearProductoComando {
        if (tiendaId == null) throw new IllegalArgumentException("La tienda es obligatoria");
        validarDatos(nombre, unidadMedida, precioContado, precioCredito);
        if (precioCredito == null) precioCredito = precioContado;
    }

    static void validarDatos(String nombre, String unidadMedida, BigDecimal precioContado, BigDecimal precioCredito) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre del producto es obligatorio");
        if (unidadMedida == null || unidadMedida.isBlank())
            throw new IllegalArgumentException("La unidad de medida es obligatoria");
        if (precioContado == null || precioContado.signum() <= 0)
            throw new IllegalArgumentException("El precio contado debe ser mayor que cero");
        if (precioCredito != null && precioCredito.compareTo(precioContado) < 0)
            throw new IllegalArgumentException("El precio a crédito no puede ser menor que el precio contado");
    }
}
