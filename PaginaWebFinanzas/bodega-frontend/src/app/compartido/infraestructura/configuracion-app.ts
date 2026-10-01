import { Injectable, signal } from '@angular/core';

/**
 * Configuración de ejecución. Se lee de /config.json al iniciar, de modo que la URL del API se puede
 * cambiar en el despliegue (Render) sin volver a compilar la aplicación.
 */
@Injectable({ providedIn: 'root' })
export class ConfiguracionApp {
  readonly apiUrl = signal('http://localhost:8080/api/v1');
  /** Muestra en el ingreso los usuarios de demostración (desactivar en producción real). */
  readonly mostrarDemo = signal(true);

  async cargar(): Promise<void> {
    try {
      const respuesta = await fetch('config.json', { cache: 'no-store' });
      if (respuesta.ok) {
        const config = (await respuesta.json()) as { apiUrl?: string; mostrarDemo?: boolean };
        if (config.apiUrl) this.apiUrl.set(config.apiUrl.replace(/\/$/, ''));
        if (config.mostrarDemo !== undefined) this.mostrarDemo.set(config.mostrarDemo);
      }
    } catch {
      // Se usa la URL por defecto
    }
  }
}
