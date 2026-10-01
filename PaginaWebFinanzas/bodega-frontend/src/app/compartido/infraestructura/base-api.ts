import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ConfiguracionApp } from './configuracion-app';

/**
 * Clase base de las fachadas de API de cada contexto: arma las URLs a partir de la configuración.
 */
export abstract class BaseApi {
  protected readonly http = inject(HttpClient);
  private readonly config = inject(ConfiguracionApp);

  protected url(ruta: string): string {
    return `${this.config.apiUrl()}${ruta}`;
  }
}

/** Extrae un mensaje legible de un error HTTP del API (cuerpo { codigo, mensaje, detalle }). */
export function mensajeDeError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const cuerpo = error.error as { mensaje?: string; detalle?: string } | null;
    if (cuerpo && typeof cuerpo === 'object' && cuerpo.mensaje) {
      return cuerpo.detalle ? `${cuerpo.mensaje}. ${cuerpo.detalle}` : cuerpo.mensaje;
    }
    if (error.status === 0) return 'No hay conexión con el servidor. Verifique que el API esté en ejecución.';
    if (error.status === 403) return 'Su rol no permite esta operación.';
    if (error.status === 404) return 'No se encontró el recurso solicitado.';
    return `Error ${error.status}: ${error.statusText}`;
  }
  return error instanceof Error ? error.message : 'Ocurrió un error inesperado.';
}
