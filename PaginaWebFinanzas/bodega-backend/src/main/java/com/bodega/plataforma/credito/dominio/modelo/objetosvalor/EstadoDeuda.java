package com.bodega.plataforma.credito.dominio.modelo.objetosvalor;

/**
 * Estado de una obligación o de una cuota. La mora no es un estado: se calcula según la fecha del sistema.
 */
public enum EstadoDeuda {
    PENDIENTE,
    PAGADA,
    CONDONADA
}
