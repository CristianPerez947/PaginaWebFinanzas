package com.bodega.plataforma.credito.dominio.servicios;

import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.EstadoDeuda;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.TasaAnual;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del modelo matemático con el caso del enunciado: compra el 15/09, corte el 20/09, pago el 26/09
 * (11 días de gracia), 3 cuotas con vencimientos el 26/10, 26/11 y 26/12. Los valores esperados se
 * verificaron aparte con una hoja de cálculo.
 */
class CalculadoraFinancieraTest {

    private static final BigDecimal TEA_24 = new BigDecimal("0.240000000");
    private static final BigDecimal TEA_36 = new BigDecimal("0.360000000");

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    @DisplayName("Tasa efectiva se conserva; nominal mensual y diaria se convierten a TEA")
    void conversionDeTasas() {
        assertEquals(bd("0.240000000"), CalculadoraFinanciera.tasaEfectivaAnual(new TasaAnual(false, bd("24"), 0)));
        // TNA 24 % capitalizable mensualmente: (1 + 0.24/12)^12 - 1
        assertEquals(bd("0.268241795"), CalculadoraFinanciera.tasaEfectivaAnual(new TasaAnual(true, bd("24"), 30)));
        // TNA 24 % capitalizable diariamente: (1 + 0.24/360)^360 - 1
        assertEquals(bd("0.271147500"), CalculadoraFinanciera.tasaEfectivaAnual(new TasaAnual(true, bd("24"), 1)));
    }

    @Test
    @DisplayName("Tasa por 11 y 30 días con TEA de 24 %")
    void tasasPorPlazo() {
        assertEquals(bd("0.006594496"), CalculadoraFinanciera.tasaPorDias(TEA_24, 11));
        assertEquals(bd("0.018087582"), CalculadoraFinanciera.tasaMensual(TEA_24));
        assertEquals(bd("0.000000000"), CalculadoraFinanciera.tasaPorDias(TEA_24, 0));
    }

    @Test
    @DisplayName("Ciclo: compra antes del corte paga el mismo mes (caso del enunciado)")
    void cicloAntesDelCorte() {
        var ciclo = CalculadoraFinanciera.calcularCiclo(LocalDateTime.of(2026, 9, 15, 10, 0), 20, LocalTime.of(23, 59), 26);
        assertEquals(LocalDate.of(2026, 9, 20), ciclo.fechaCorte());
        assertEquals(LocalDate.of(2026, 9, 26), ciclo.fechaPago());
    }

    @Test
    @DisplayName("Ciclo: compra después del corte pasa al mes siguiente")
    void cicloDespuesDelCorte() {
        var ciclo = CalculadoraFinanciera.calcularCiclo(LocalDateTime.of(2026, 9, 22, 9, 0), 20, LocalTime.of(23, 59), 26);
        assertEquals(LocalDate.of(2026, 10, 20), ciclo.fechaCorte());
        assertEquals(LocalDate.of(2026, 10, 26), ciclo.fechaPago());
    }

    @Test
    @DisplayName("Ciclo: la hora de corte decide el día del corte")
    void cicloEnElLimiteDeLaHora() {
        var enElLimite = CalculadoraFinanciera.calcularCiclo(LocalDateTime.of(2026, 9, 20, 23, 59, 0), 20, LocalTime.of(23, 59), 26);
        var pasadoElLimite = CalculadoraFinanciera.calcularCiclo(LocalDateTime.of(2026, 9, 20, 23, 59, 1), 20, LocalTime.of(23, 59), 26);
        assertEquals(LocalDate.of(2026, 9, 26), enElLimite.fechaPago());
        assertEquals(LocalDate.of(2026, 10, 26), pasadoElLimite.fechaPago());
    }

    @Test
    @DisplayName("Ciclo: si el día de pago es menor que el de corte, se paga el mes siguiente al corte")
    void cicloConPagoAntesQueCorte() {
        var ciclo = CalculadoraFinancieraTestAyuda.ciclo(LocalDateTime.of(2026, 9, 15, 10, 0), 25, 10);
        assertEquals(LocalDate.of(2026, 9, 25), ciclo.fechaCorte());
        assertEquals(LocalDate.of(2026, 10, 10), ciclo.fechaPago());
    }

    @Test
    @DisplayName("Fin de mes: interés por 11 días sobre S/ 500.00")
    void obligacionFinDeMesConInteres() {
        var o = obligacionFinDeMes();
        assertEquals(11, o.getDiasGracia());
        assertEquals(bd("500.00"), o.getCapital());
        assertEquals(bd("3.30"), o.getInteresCompensatorio());
        assertEquals(bd("503.30"), o.montoFinDeMes());
        assertEquals(bd("503.30"), o.totalFinDeMes(LocalDate.of(2026, 9, 26)));
    }

    @Test
    @DisplayName("Fin de mes: pago con 5 días de mora agrega intereses moratorios y prelación mora, interés, capital")
    void obligacionFinDeMesConMora() {
        var o = obligacionFinDeMes();
        var fechaPagoReal = LocalDate.of(2026, 10, 1);
        assertTrue(o.tieneMora(fechaPagoReal));
        assertFalse(o.tieneMora(LocalDate.of(2026, 9, 26)));
        assertEquals(bd("2.15"), o.moraFinDeMes(fechaPagoReal));
        var pago = o.pagarFinDeMes(fechaPagoReal);
        assertEquals(bd("2.15"), pago.getMontoMora());
        assertEquals(bd("3.30"), pago.getMontoInteres());
        assertEquals(bd("500.00"), pago.getMontoCapital());
        assertEquals(bd("505.45"), pago.total());
        assertEquals(EstadoDeuda.PAGADA, o.getEstado());
        assertThrows(IllegalStateException.class, () -> o.pagarFinDeMes(fechaPagoReal), "no se paga dos veces");
    }

    @Test
    @DisplayName("Cuotas: se capitaliza la gracia (S/ 301.98) y el cronograma francés cuadra")
    void cronogramaFrancesDelEnunciado() {
        var o = obligacionEnTresCuotas();
        assertEquals(11, o.getDiasGracia());
        assertEquals(bd("1.98"), o.getInteresCompensatorio());
        assertEquals(3, o.getCuotas().size());

        var c1 = o.getCuotas().get(0);
        var c2 = o.getCuotas().get(1);
        var c3 = o.getCuotas().get(2);

        assertEquals(LocalDate.of(2026, 10, 26), c1.getFechaVencimiento());
        assertEquals(LocalDate.of(2026, 11, 26), c2.getFechaVencimiento());
        assertEquals(LocalDate.of(2026, 12, 26), c3.getFechaVencimiento());

        assertEquals(bd("104.32"), c1.getCuota());
        assertEquals(bd("5.46"), c1.getInteres());
        assertEquals(bd("98.86"), c1.getAmortizacion());
        assertEquals(bd("203.12"), c1.getSaldo());

        assertEquals(bd("3.67"), c2.getInteres());
        assertEquals(bd("100.65"), c2.getAmortizacion());
        assertEquals(bd("102.47"), c2.getSaldo());

        assertEquals(bd("104.32"), c3.getCuota());
        assertEquals(bd("1.85"), c3.getInteres());
        assertEquals(bd("102.47"), c3.getAmortizacion());
        assertEquals(bd("0.00"), c3.getSaldo());

        var totalAmortizado = o.getCuotas().stream().map(c -> c.getAmortizacion()).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(bd("301.98"), totalAmortizado, "las amortizaciones suman el capital capitalizado");
    }

    @Test
    @DisplayName("Cuotas: se pagan en orden; la cuota tardía suma mora sobre la cuota")
    void pagoDeCuotas() {
        var o = obligacionEnTresCuotas();
        assertThrows(IllegalStateException.class, () -> o.pagarCuota(2, LocalDate.of(2026, 10, 26)),
                "no se puede saltar la cuota 1");
        assertEquals(bd("301.98"), o.capitalPendiente());

        var pago1 = o.pagarCuota(1, LocalDate.of(2026, 10, 26));
        assertEquals(bd("104.32"), pago1.total());
        assertEquals(bd("0.00"), pago1.getMontoMora());
        assertEquals(bd("203.12"), o.capitalPendiente());

        // cuota 2 vence el 26/11; se paga el 6/12 (10 días de mora, TEA de mora 36 %)
        var fechaTardia = LocalDate.of(2026, 12, 6);
        assertTrue(o.tieneMora(fechaTardia));
        var pago2 = o.pagarCuota(2, fechaTardia);
        assertEquals(bd("104.32"), pago2.getMontoInteres().add(pago2.getMontoCapital()));
        assertEquals(bd("0.89"), pago2.getMontoMora());

        o.pagarCuota(3, LocalDate.of(2026, 12, 26));
        assertEquals(EstadoDeuda.PAGADA, o.getEstado());
        assertEquals(bd("0.00"), o.capitalPendiente());
    }

    @Test
    @DisplayName("Condonar exige motivo, conserva el historial y deja la obligación sin saldo")
    void condonacion() {
        var o = obligacionEnTresCuotas();
        assertThrows(IllegalArgumentException.class, () -> o.condonar(" ", LocalDate.of(2026, 9, 30)));
        o.condonar("Cliente en situación de emergencia", LocalDate.of(2026, 9, 30));
        assertEquals(EstadoDeuda.CONDONADA, o.getEstado());
        assertTrue(o.getCuotas().stream().allMatch(c -> c.getEstado() == EstadoDeuda.CONDONADA));
        assertEquals(bd("0.00"), o.capitalPendiente());
        assertFalse(o.tieneMora(LocalDate.of(2027, 1, 1)));
    }

    private static Obligacion obligacionFinDeMes() {
        var compra = LocalDateTime.of(2026, 9, 15, 10, 0);
        var ciclo = CalculadoraFinanciera.calcularCiclo(compra, 20, LocalTime.of(23, 59), 26);
        return Obligacion.finDeMes(1L, 1L, "PEN", bd("500.00"), compra, ciclo, TEA_24, TEA_36);
    }

    private static Obligacion obligacionEnTresCuotas() {
        var compra = LocalDateTime.of(2026, 9, 15, 10, 0);
        var ciclo = CalculadoraFinanciera.calcularCiclo(compra, 20, LocalTime.of(23, 59), 26);
        return Obligacion.enCuotas(1L, 1L, "PEN", bd("300.00"), compra, ciclo, TEA_24, TEA_36, 3);
    }

    /** Ayuda para escribir la prueba del ciclo con día de pago menor que el de corte. */
    private static final class CalculadoraFinancieraTestAyuda {
        static com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Ciclo ciclo(LocalDateTime compra, int corte, int pago) {
            return CalculadoraFinanciera.calcularCiclo(compra, corte, LocalTime.of(23, 59), pago);
        }
    }
}
