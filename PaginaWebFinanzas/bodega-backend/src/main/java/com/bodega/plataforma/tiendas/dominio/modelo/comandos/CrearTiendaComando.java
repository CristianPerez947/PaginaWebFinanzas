package com.bodega.plataforma.tiendas.dominio.modelo.comandos;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Comando para dar de alta una tienda junto con el usuario de su dueño/vendedor.
 *
 * @param nombre          nombre comercial de la tienda
 * @param slug            identificador en la URL (minúsculas, dígitos y guiones)
 * @param logo            nombre del archivo del logo (carpeta assets del frontend); puede ser nulo
 * @param ruc             RUC (11 dígitos); opcional
 * @param direccion       dirección de la tienda
 * @param telefono        teléfono de contacto
 * @param tipoCambioUsd   tipo de cambio soles por dólar; por defecto 3.75
 * @param nombreDueno     nombre del dueño
 * @param usuarioVendedor usuario de acceso del vendedor
 * @param claveVendedor   clave inicial del vendedor (mínimo 6 caracteres)
 */
public record CrearTiendaComando(String nombre, String slug, String logo, String ruc, String direccion,
                                 String telefono, BigDecimal tipoCambioUsd, String nombreDueno,
                                 String usuarioVendedor, String claveVendedor) {

    private static final Pattern PATRON_SLUG = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    public CrearTiendaComando {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre de la tienda es obligatorio");
        if (slug == null || !PATRON_SLUG.matcher(slug).matches())
            throw new IllegalArgumentException("El identificador de URL solo admite minúsculas, dígitos y guiones");
        if (ruc != null && !ruc.isBlank() && !ruc.matches("\\d{11}"))
            throw new IllegalArgumentException("El RUC debe tener 11 dígitos");
        if (nombreDueno == null || nombreDueno.isBlank()) throw new IllegalArgumentException("El nombre del dueño es obligatorio");
        if (usuarioVendedor == null || usuarioVendedor.isBlank())
            throw new IllegalArgumentException("El usuario del vendedor es obligatorio");
        if (claveVendedor == null || claveVendedor.length() < 6)
            throw new IllegalArgumentException("La clave del vendedor debe tener al menos 6 caracteres");
        if (tipoCambioUsd == null) tipoCambioUsd = new BigDecimal("3.7500");
        if (tipoCambioUsd.signum() <= 0) throw new IllegalArgumentException("El tipo de cambio debe ser mayor que cero");
    }
}
