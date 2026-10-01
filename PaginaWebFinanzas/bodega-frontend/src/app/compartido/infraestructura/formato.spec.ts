import { describe, expect, it } from 'vitest';
import { formatearFecha, formatearFechaHora, formatearMoneda, formatearTasa } from './formato';

describe('formato', () => {
  it('formatea soles y dólares con 2 decimales', () => {
    expect(formatearMoneda(1234.5)).toBe('S/ 1,234.50');
    expect(formatearMoneda(16, 'USD')).toBe('US$ 16.00');
    expect(formatearMoneda(null)).toBe('S/ 0.00');
  });

  it('formatea fechas ISO como dd/MM/yyyy', () => {
    expect(formatearFecha('2026-09-26')).toBe('26/09/2026');
    expect(formatearFecha('2026-09-26T10:30:00')).toBe('26/09/2026');
    expect(formatearFecha(null)).toBe('');
  });

  it('formatea fecha y hora', () => {
    expect(formatearFechaHora('2026-09-26T10:30:15')).toBe('26/09/2026 10:30');
  });

  it('formatea tasas con hasta 7 decimales', () => {
    expect(formatearTasa(24)).toBe('24.00 %');
    expect(formatearTasa(34.4888824)).toBe('34.4888824 %');
  });
});
