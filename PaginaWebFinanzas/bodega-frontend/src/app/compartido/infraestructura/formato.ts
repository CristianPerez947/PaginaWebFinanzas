import { Pipe, PipeTransform } from '@angular/core';

/** Formatea un importe con su moneda (S/ para soles, US$ para dólares) y 2 decimales. */
export function formatearMoneda(monto: number | null | undefined, moneda: string | null | undefined = 'PEN'): string {
  const valor = monto ?? 0;
  const simbolo = moneda === 'USD' ? 'US$' : 'S/';
  return `${simbolo} ${valor.toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

/** Formatea una fecha ISO (yyyy-MM-dd o fecha-hora) como dd/MM/yyyy. */
export function formatearFecha(iso: string | null | undefined): string {
  if (!iso) return '';
  const [fecha] = iso.split('T');
  const [a, m, d] = fecha.split('-');
  return `${d}/${m}/${a}`;
}

export function formatearFechaHora(iso: string | null | undefined): string {
  if (!iso) return '';
  const [fecha, hora] = iso.split('T');
  return `${formatearFecha(fecha)} ${(hora ?? '').substring(0, 5)}`;
}

/** Fecha local de hoy como yyyy-MM-dd (para campos de fecha). */
export function hoyIso(): string {
  const ahora = new Date();
  const mes = `${ahora.getMonth() + 1}`.padStart(2, '0');
  const dia = `${ahora.getDate()}`.padStart(2, '0');
  return `${ahora.getFullYear()}-${mes}-${dia}`;
}

export function formatearTasa(porcentaje: number | null | undefined): string {
  return `${(porcentaje ?? 0).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 7 })} %`;
}

@Pipe({ name: 'moneda', standalone: true })
export class MonedaPipe implements PipeTransform {
  transform(monto: number | null | undefined, moneda: string | null | undefined = 'PEN'): string {
    return formatearMoneda(monto, moneda);
  }
}

@Pipe({ name: 'fecha', standalone: true })
export class FechaPipe implements PipeTransform {
  transform(iso: string | null | undefined): string {
    return formatearFecha(iso);
  }
}

@Pipe({ name: 'fechaHora', standalone: true })
export class FechaHoraPipe implements PipeTransform {
  transform(iso: string | null | undefined): string {
    return formatearFechaHora(iso);
  }
}

@Pipe({ name: 'tasa', standalone: true })
export class TasaPipe implements PipeTransform {
  transform(porcentaje: number | null | undefined): string {
    return formatearTasa(porcentaje);
  }
}

/** Convierte la fecha de la respuesta para descargar un archivo Blob. */
export function descargarBlob(blob: Blob, nombre: string): void {
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombre;
  enlace.click();
  URL.revokeObjectURL(url);
}
