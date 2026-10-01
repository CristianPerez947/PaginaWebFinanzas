package com.bodega.plataforma.clientes.dominio.modelo.objetosvalor;

/**
 * Periodo de capitalización de una tasa nominal (base 360 días).
 */
public enum Capitalizacion {
    DIARIA(1),
    MENSUAL(30);

    private final int dias;

    Capitalizacion(int dias) {
        this.dias = dias;
    }

    public int dias() {
        return dias;
    }
}
