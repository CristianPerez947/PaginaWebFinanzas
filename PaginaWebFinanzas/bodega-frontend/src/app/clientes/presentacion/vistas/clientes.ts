import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { MonedaPipe, TasaPipe } from '../../../compartido/infraestructura/formato';
import { confirmar } from '../../../compartido/presentacion/componentes/confirmar-dialogo';
import { TiendasApi } from '../../../tiendas/infraestructura/tiendas-api';
import { ClientesStore } from '../../aplicacion/clientes-store';
import { Cliente, nombreCompleto } from '../../dominio/modelo/cliente.entidad';
import { ClientesApi } from '../../infraestructura/clientes-api';
import { ClaveDialogo } from '../componentes/clave-dialogo';
import { ClienteFormDialogo, ResultadoClienteForm } from '../componentes/cliente-form-dialogo';

/**
 * Gestión de clientes de la bodega: alta y baja, condiciones de crédito y claves de acceso.
 */
@Component({
  selector: 'app-clientes',
  imports: [FormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MonedaPipe, TasaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Clientes</h1>
          <p>Vecinos con crédito en tu bodega. Cada uno tiene sus propias tasas, límite y fechas.</p>
        </div>
        <button mat-flat-button color="primary" (click)="nuevo()"><mat-icon>person_add</mat-icon> Nuevo cliente</button>
      </div>

      <mat-form-field class="buscador">
        <mat-label>Buscar por nombre o DNI</mat-label>
        <mat-icon matPrefix>search</mat-icon>
        <input matInput [ngModel]="busqueda()" (ngModelChange)="busqueda.set($event)" />
      </mat-form-field>

      @if (store.error()) { <div class="aviso-error">{{ store.error() }}</div> }

      <div class="tabla-contenedor">
        <table class="tabla">
          <thead>
            <tr>
              <th>Cliente</th><th>Teléfono</th><th class="num">Límite</th><th>Tasa</th>
              <th>Corte / Pago</th><th>Estado</th><th></th>
            </tr>
          </thead>
          <tbody>
            @for (c of filtrados(); track c.id) {
              <tr [class.baja]="!c.activo">
                <td>
                  <a class="enlace" [routerLink]="['/vendedor/clientes', c.id]"><strong>{{ nombre(c) }}</strong></a>
                  <div class="texto-suave">DNI {{ c.dni }}</div>
                </td>
                <td>{{ c.telefono }}</td>
                <td class="num">{{ c.limiteCredito | moneda: c.moneda }}<div class="texto-suave">hasta {{ c.maxMeses }} meses</div></td>
                <td>
                  {{ c.tasaCompensatoria | tasa }} {{ c.tipoTasa === 'EFECTIVA' ? 'TEA' : 'TNA' }}
                  <div class="texto-suave">Mora {{ c.tasaMora | tasa }}</div>
                </td>
                <td>Día {{ c.diaCorte }} / día {{ c.diaPago }}</td>
                <td>
                  <span class="etiqueta-estado" [class.baja]="!c.activo">{{ c.activo ? 'Activo' : 'De baja' }}</span>
                </td>
                <td>
                  <div class="acciones">
                    <a mat-icon-button [routerLink]="['/vendedor/clientes', c.id]" title="Ver deuda y pagos" aria-label="Ver deuda y pagos"><mat-icon>account_balance_wallet</mat-icon></a>
                    <button mat-icon-button (click)="editar(c)" title="Editar" aria-label="Editar"><mat-icon>edit</mat-icon></button>
                    <button mat-icon-button (click)="regenerarClave(c)" [disabled]="!c.activo" title="Generar nueva clave" aria-label="Generar nueva clave"><mat-icon>key</mat-icon></button>
                    <button mat-icon-button (click)="cambiarEstado(c)" [title]="c.activo ? 'Dar de baja' : 'Reactivar'" [attr.aria-label]="c.activo ? 'Dar de baja' : 'Reactivar'">
                      <mat-icon>{{ c.activo ? 'person_off' : 'person_check' }}</mat-icon>
                    </button>
                  </div>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="7" class="vacio"><mat-icon>groups</mat-icon><p>{{ store.cargando() ? 'Cargando…' : 'No hay clientes que coincidan.' }}</p></td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: `
    .buscador {
      width: 360px;
      max-width: 100%;
      margin-bottom: 12px;
    }
    tr.baja td {
      opacity: 0.6;
    }
    .enlace {
      color: var(--verde);
      text-decoration: none;
    }
  `,
})
export class Clientes {
  protected readonly store = inject(ClientesStore);
  private readonly api = inject(ClientesApi);
  private readonly dialogo = inject(MatDialog);
  private readonly avisos = inject(MatSnackBar);
  private readonly tiendas = inject(TiendasApi);
  private readonly router = inject(Router);

  protected readonly busqueda = signal('');
  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.store
      .clientes()
      .filter((c) => !texto || `${nombreCompleto(c)} ${c.dni}`.toLowerCase().includes(texto));
  });

  constructor() {
    this.store.cargar();
  }

  protected nombre(c: Cliente): string {
    return nombreCompleto(c);
  }

  protected nuevo(): void {
    this.dialogo
      .open(ClienteFormDialogo, { width: '760px', maxWidth: '96vw', data: null })
      .afterClosed()
      .subscribe((r?: ResultadoClienteForm) => {
        if (!r) return;
        this.store.cargar();
        if (r.clave) this.mostrarClave(r.cliente, r.clave);
      });
  }

  protected editar(cliente: Cliente): void {
    this.dialogo
      .open(ClienteFormDialogo, { width: '760px', maxWidth: '96vw', data: cliente })
      .afterClosed()
      .subscribe((r?: ResultadoClienteForm) => {
        if (r) {
          this.store.cargar();
          this.avisos.open('Cliente actualizado', 'Cerrar');
        }
      });
  }

  protected regenerarClave(cliente: Cliente): void {
    confirmar(this.dialogo, {
      titulo: 'Generar nueva clave',
      mensaje: `Se generará una nueva clave para ${nombreCompleto(cliente)}. La clave anterior dejará de funcionar.`,
      textoConfirmar: 'Generar clave',
    }).subscribe((ok) => {
      if (!ok) return;
      this.api.regenerarClave(cliente.id).subscribe({
        next: (r) => this.mostrarClave(r.cliente, r.clave),
        error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar'),
      });
    });
  }

  protected cambiarEstado(cliente: Cliente): void {
    const baja = cliente.activo;
    confirmar(this.dialogo, {
      titulo: baja ? 'Dar de baja al cliente' : 'Reactivar al cliente',
      mensaje: baja
        ? `${nombreCompleto(cliente)} ya no podrá ingresar ni recibir crédito. Su historial se conserva.`
        : `${nombreCompleto(cliente)} volverá a poder ingresar y recibir crédito.`,
      textoConfirmar: baja ? 'Dar de baja' : 'Reactivar',
      peligro: baja,
    }).subscribe((ok) => {
      if (!ok) return;
      this.api.cambiarEstado(cliente.id, !cliente.activo).subscribe({
        next: () => this.store.cargar(),
        error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar'),
      });
    });
  }

  private mostrarClave(cliente: Cliente, clave: string): void {
    this.tiendas.miTienda().subscribe({
      next: (tienda) =>
        this.dialogo.open(ClaveDialogo, {
          width: '520px',
          data: {
            cliente,
            clave,
            nombreTienda: tienda.nombre,
            urlTienda: `${window.location.origin}${this.router.serializeUrl(this.router.createUrlTree(['/t', tienda.slug]))}`,
          },
        }),
      error: () =>
        this.dialogo.open(ClaveDialogo, {
          width: '520px',
          data: { cliente, clave, nombreTienda: 'tu bodega', urlTienda: window.location.origin },
        }),
    });
  }
}
