import { Injectable, inject, signal } from '@angular/core';
import { mensajeDeError } from '../../compartido/infraestructura/base-api';
import { TiendaPublica } from '../dominio/modelo/tienda.entidad';
import { TiendasApi } from '../infraestructura/tiendas-api';

/**
 * Tienda cuya página pública se está visitando (según el identificador de la URL /t/:slug).
 */
@Injectable({ providedIn: 'root' })
export class TiendaActualStore {
  private readonly api = inject(TiendasApi);

  private readonly tiendaSenal = signal<TiendaPublica | null>(null);
  private readonly errorSenal = signal<string | null>(null);
  private readonly cargandoSenal = signal(false);

  readonly tienda = this.tiendaSenal.asReadonly();
  readonly error = this.errorSenal.asReadonly();
  readonly cargando = this.cargandoSenal.asReadonly();

  cargar(slug: string): void {
    if (this.tiendaSenal()?.slug === slug) return;
    this.tiendaSenal.set(null);
    this.errorSenal.set(null);
    this.cargandoSenal.set(true);
    this.api.obtenerPublica(slug).subscribe({
      next: (tienda) => {
        this.tiendaSenal.set(tienda);
        this.cargandoSenal.set(false);
      },
      error: (e) => {
        this.errorSenal.set(mensajeDeError(e));
        this.cargandoSenal.set(false);
      },
    });
  }
}
