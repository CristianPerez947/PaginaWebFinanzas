import { Injectable, inject, signal } from '@angular/core';
import { mensajeDeError } from '../../compartido/infraestructura/base-api';
import { Cliente } from '../dominio/modelo/cliente.entidad';
import { ClientesApi } from '../infraestructura/clientes-api';

/**
 * Estado de la lista de clientes de la tienda del vendedor.
 */
@Injectable({ providedIn: 'root' })
export class ClientesStore {
  private readonly api = inject(ClientesApi);

  private readonly clientesSenal = signal<Cliente[]>([]);
  private readonly cargandoSenal = signal(false);
  private readonly errorSenal = signal<string | null>(null);

  readonly clientes = this.clientesSenal.asReadonly();
  readonly cargando = this.cargandoSenal.asReadonly();
  readonly error = this.errorSenal.asReadonly();

  cargar(): void {
    this.cargandoSenal.set(true);
    this.errorSenal.set(null);
    this.api.listar().subscribe({
      next: (c) => {
        this.clientesSenal.set(c);
        this.cargandoSenal.set(false);
      },
      error: (e) => {
        this.errorSenal.set(mensajeDeError(e));
        this.cargandoSenal.set(false);
      },
    });
  }
}
