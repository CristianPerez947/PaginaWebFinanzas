import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { confirmar } from '../../../compartido/presentacion/componentes/confirmar-dialogo';
import { ImagenFallback } from '../../../compartido/presentacion/componentes/imagen-fallback';
import { Tienda } from '../../dominio/modelo/tienda.entidad';
import { TiendasApi } from '../../infraestructura/tiendas-api';
import { TiendaFormDialogo } from '../componentes/tienda-form-dialogo';

/**
 * Administración de tiendas (administrador del sistema): alta y baja de tiendas y de sus dueños.
 */
@Component({
  selector: 'app-admin-tiendas',
  imports: [RouterLink, MatButtonModule, MatIconModule, ImagenFallback],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Tiendas</h1>
          <p>Da de alta las bodegas que usan la plataforma y entrega a cada dueño su usuario y clave.</p>
        </div>
        <button mat-flat-button color="primary" (click)="abrir(null)"><mat-icon>add_business</mat-icon> Nueva tienda</button>
      </div>
      @if (error()) { <div class="aviso-error">{{ error() }}</div> }

      <div class="tabla-contenedor">
        <table class="tabla">
          <thead><tr><th></th><th>Tienda</th><th>Dueño / usuario</th><th>Contacto</th><th class="num">T. cambio</th><th>Estado</th><th></th></tr></thead>
          <tbody>
            @for (t of tiendas(); track t.id) {
              <tr [class.baja]="!t.activa">
                <td><div class="logo"><app-imagen [src]="t.logo ? 'assets/logos/' + t.logo : null" icono="storefront" [alt]="t.nombre" /></div></td>
                <td>
                  <strong>{{ t.nombre }}</strong>
                  <div class="texto-suave">/t/{{ t.slug }} @if (t.ruc) { · RUC {{ t.ruc }} }</div>
                </td>
                <td>{{ t.nombreDueno }}<div class="texto-suave">usuario: {{ t.usuarioVendedor }}</div></td>
                <td>{{ t.telefono }}<div class="texto-suave">{{ t.direccion }}</div></td>
                <td class="num">{{ t.tipoCambioUsd }}</td>
                <td><span class="etiqueta-estado" [class.baja]="!t.activa">{{ t.activa ? 'Activa' : 'De baja' }}</span></td>
                <td>
                  <div class="acciones">
                    <a mat-icon-button [routerLink]="['/t', t.slug]" target="_blank" title="Ver página pública" aria-label="Ver página pública"><mat-icon>open_in_new</mat-icon></a>
                    <button mat-icon-button (click)="abrir(t)" title="Editar" aria-label="Editar"><mat-icon>edit</mat-icon></button>
                    <button mat-icon-button (click)="cambiarEstado(t)" [title]="t.activa ? 'Dar de baja' : 'Reactivar'" [attr.aria-label]="t.activa ? 'Dar de baja' : 'Reactivar'">
                      <mat-icon>{{ t.activa ? 'block' : 'check_circle' }}</mat-icon>
                    </button>
                  </div>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="7" class="vacio"><mat-icon>storefront</mat-icon><p>Aún no hay tiendas.</p></td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: `
    .logo {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      overflow: hidden;
    }
    tr.baja td {
      opacity: 0.6;
    }
  `,
})
export class AdminTiendas {
  private readonly api = inject(TiendasApi);
  private readonly dialogo = inject(MatDialog);
  private readonly avisos = inject(MatSnackBar);

  protected readonly tiendas = signal<Tienda[]>([]);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.cargar();
  }

  private cargar(): void {
    this.api.listar().subscribe({
      next: (t) => this.tiendas.set(t),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected abrir(tienda: Tienda | null): void {
    this.dialogo
      .open(TiendaFormDialogo, { width: '720px', maxWidth: '96vw', data: tienda })
      .afterClosed()
      .subscribe((t?: Tienda) => {
        if (t) {
          this.avisos.open(tienda ? 'Tienda actualizada' : 'Tienda creada. Entrega al dueño su usuario y clave.', 'Cerrar');
          this.cargar();
        }
      });
  }

  protected cambiarEstado(tienda: Tienda): void {
    const baja = tienda.activa;
    confirmar(this.dialogo, {
      titulo: baja ? 'Dar de baja la tienda' : 'Reactivar la tienda',
      mensaje: baja
        ? `“${tienda.nombre}” y todos sus usuarios (vendedor y clientes) no podrán ingresar. La información se conserva.`
        : `“${tienda.nombre}” y sus usuarios podrán ingresar de nuevo.`,
      textoConfirmar: baja ? 'Dar de baja' : 'Reactivar',
      peligro: baja,
    }).subscribe((ok) => {
      if (!ok) return;
      this.api.cambiarEstado(tienda.id, !tienda.activa).subscribe({
        next: () => this.cargar(),
        error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar'),
      });
    });
  }
}
