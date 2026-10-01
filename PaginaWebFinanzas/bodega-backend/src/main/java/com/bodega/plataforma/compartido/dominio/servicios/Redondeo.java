package com.bodega.plataforma.compartido.dominio.servicios;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Convenciones de redondeo del Anexo A: dinero con 2 decimales; tasas con al menos 7 decimales.
 */
public final class Redondeo {

    public static final int DECIMALES_DINERO = 2;
    public static final int DECIMALES_TASA = 9;
    public static final MathContext CONTEXTO_CALCULO = new MathContext(40, RoundingMode.HALF_UP);

    private Redondeo() {
    }

    public static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(DECIMALES_DINERO, RoundingMode.HALF_UP);
    }

    public static BigDecimal tasa(BigDecimal valor) {
        return valor.setScale(DECIMALES_TASA, RoundingMode.HALF_UP);
    }
}
