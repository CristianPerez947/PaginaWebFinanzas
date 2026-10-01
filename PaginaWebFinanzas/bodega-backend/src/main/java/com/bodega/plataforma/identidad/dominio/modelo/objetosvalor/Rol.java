package com.bodega.plataforma.identidad.dominio.modelo.objetosvalor;

/**
 * Roles del sistema. Cada usuario tiene exactamente uno.
 */
public enum Rol {
    /** Administra la plataforma: tiendas y sus dueños. */
    ADMIN_SISTEMA,
    /** Dueño/vendedor de una bodega: gestiona clientes, productos, ventas y cobranzas de su tienda. */
    VENDEDOR,
    /** Cliente con crédito: solo consulta su deuda e historial. */
    CLIENTE
}
