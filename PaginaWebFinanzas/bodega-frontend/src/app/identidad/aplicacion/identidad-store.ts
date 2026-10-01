import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { Sesion } from '../dominio/modelo/sesion.entidad';
import { IdentidadApi } from '../infraestructura/identidad-api';

const CLAVE_SESION = 'bodega.sesion';

/**
 * Estado de la sesión del usuario. La sesión se conserva en el navegador para sobrevivir a una recarga.
 */
@Injectable({ providedIn: 'root' })
export class IdentidadStore {
  private readonly api = inject(IdentidadApi);
  private readonly router = inject(Router);

  private readonly sesionSenal = signal<Sesion | null>(this.leer());

  readonly sesion = this.sesionSenal.asReadonly();
  readonly autenticado = computed(() => this.sesionSenal() !== null);
  readonly rol = computed(() => this.sesionSenal()?.rol ?? null);
  readonly token = computed(() => this.sesionSenal()?.token ?? null);

  iniciarSesionPersonal(usuario: string, clave: string): Observable<Sesion> {
    return this.api.iniciarSesion(usuario, clave).pipe(tap((s) => this.guardar(s)));
  }

  iniciarSesionCliente(tiendaId: number, slug: string, dni: string, clave: string): Observable<Sesion> {
    return this.api.iniciarSesionCliente(tiendaId, dni, clave).pipe(tap((s) => this.guardar({ ...s, slug })));
  }

  /** Cierra la sesión y lleva al usuario a la pantalla de ingreso que le corresponde. */
  cerrarSesion(): void {
    const sesion = this.sesionSenal();
    this.guardar(null);
    if (sesion?.rol === 'CLIENTE' && sesion.slug) this.router.navigate(['/t', sesion.slug]);
    else this.router.navigate(['/ingresar']);
  }

  /** Limpia la sesión sin redirigir (lo usa el interceptor ante un 401). */
  invalidar(): Sesion | null {
    const sesion = this.sesionSenal();
    this.guardar(null);
    return sesion;
  }

  rutaInicio(): string {
    switch (this.rol()) {
      case 'ADMIN_SISTEMA':
        return '/admin/tiendas';
      case 'VENDEDOR':
        return '/vendedor/panel';
      case 'CLIENTE':
        return `/t/${this.sesionSenal()?.slug ?? ''}/mi-cuenta`;
      default:
        return '/';
    }
  }

  private guardar(sesion: Sesion | null): void {
    this.sesionSenal.set(sesion);
    try {
      if (sesion) localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
      else localStorage.removeItem(CLAVE_SESION);
    } catch {
      // Sin almacenamiento local: la sesión vive solo en memoria
    }
  }

  private leer(): Sesion | null {
    try {
      const texto = localStorage.getItem(CLAVE_SESION);
      return texto ? (JSON.parse(texto) as Sesion) : null;
    } catch {
      return null;
    }
  }
}
