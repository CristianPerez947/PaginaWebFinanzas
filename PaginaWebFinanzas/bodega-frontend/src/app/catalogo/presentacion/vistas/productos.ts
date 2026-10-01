import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { MonedaPipe } from '../../../compartido/infraestructura/formato';
import { confirmar } from '../../../compartido/presentacion/componentes/confirmar-dialogo';
import { ImagenFallback } from '../../../compartido/presentacion/componentes/imagen-fallback';
import { Producto } from '../../dominio/modelo/producto.entidad';
import { CatalogoApi } from '../../infraestructura/catalogo-api';
import { ProductoFormDialogo } from '../componentes/producto-form-dialogo';

/**
 * Gestión del catálogo de la bodega: alta, edición y baja lógica de productos.
 */
@Component({
  selector: 'app-productos',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MonedaPipe, ImagenFallback],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Productos</h1>
          <p>{{ productos().length }} productos en tu catálogo. Las imágenes se toman de la carpeta assets/productos.</p>
        </div>
        <button mat-flat-button color="primary" (click)="nuevo()"><mat-icon>add</mat-icon> Nuevo producto</button>
      </div>

      <mat-form-field class="buscador">
        <mat-label>Buscar producto</mat-label>
        <mat-icon matPrefix>search</mat-icon>
        <input matInput [ngModel]="busqueda()" (ngModelChange)="busqueda.set($event)" />
      </mat-form-field>

      @if (error()) { <div class="aviso-error">{{ error() }}</div> }

      <div class="tabla-contenedor">
        <table class="tabla">
          <thead>
            <tr><th></th><th>Producto</th><th>Categoría</th><th class="num">Contado</th><th class="num">Crédito</th><th>Cuotas</th><th>Estado</th><th></th></tr>
          </thead>
          <tbody>
            @for (p of filtrados(); track p.id) {
              <tr [class.baja]="!p.activo">
                <td><div class="mini"><app-imagen [src]="p.imagen ? 'assets/productos/' + p.imagen : null" [alt]="p.nombre" /></div></td>
                <td><strong>{{ p.nombre }}</strong><div class="texto-suave">{{ p.marca }} · por {{ p.unidadMedida }}</div></td>
                <td>{{ p.categoria }}</td>
                <td class="num">{{ p.precioContado | moneda }}</td>
                <td class="num">{{ p.precioCredito | moneda }}</td>
                <td>@if (p.permiteCuotas) { <span class="etiqueta-estado neutra">Sí</span> } @else { <span class="texto-suave">No</span> }</td>
                <td><span class="etiqueta-estado" [class.baja]="!p.activo">{{ p.activo ? 'Activo' : 'De baja' }}</span></td>
                <td>
                  <div class="acciones">
                    <button mat-icon-button (click)="editar(p)" title="Editar" aria-label="Editar"><mat-icon>edit</mat-icon></button>
                    <button mat-icon-button (click)="cambiarEstado(p)" [title]="p.activo ? 'Dar de baja' : 'Reactivar'" [attr.aria-label]="p.activo ? 'Dar de baja' : 'Reactivar'">
                      <mat-icon>{{ p.activo ? 'visibility_off' : 'visibility' }}</mat-icon>
                    </button>
                  </div>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="8" class="vacio"><mat-icon>inventory_2</mat-icon><p>{{ cargando() ? 'Cargando…' : 'No hay productos que coincidan.' }}</p></td></tr>
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
    .mini {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      overflow: hidden;
    }
    .mini :host ::ng-deep mat-icon {
      font-size: 22px;
    }
    tr.baja td {
      opacity: 0.6;
    }
  `,
})
export class Productos {
  private readonly api = inject(CatalogoApi);
  private readonly dialogo = inject(MatDialog);
  private readonly avisos = inject(MatSnackBar);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly busqueda = signal('');

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.productos().filter((p) => !texto || `${p.nombre} ${p.marca ?? ''} ${p.categoria ?? ''}`.toLowerCase().includes(texto));
  });

  constructor() {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.api.listar().subscribe({
      next: (p) => {
        this.productos.set(p);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      },
    });
  }

  protected nuevo(): void {
    this.abrir(null);
  }

  protected editar(producto: Producto): void {
    this.abrir(producto);
  }

  private abrir(producto: Producto | null): void {
    this.dialogo
      .open(ProductoFormDialogo, { width: '720px', maxWidth: '96vw', data: producto })
      .afterClosed()
      .subscribe((p?: Producto) => {
        if (p) {
          this.avisos.open(producto ? 'Producto actualizado' : 'Producto creado', 'Cerrar');
          this.cargar();
        }
      });
  }

  protected cambiarEstado(producto: Producto): void {
    const baja = producto.activo;
    confirmar(this.dialogo, {
      titulo: baja ? 'Dar de baja el producto' : 'Reactivar el producto',
      mensaje: baja
        ? `“${producto.nombre}” dejará de mostrarse en el catálogo y no se podrá vender. El historial de ventas se conserva.`
        : `“${producto.nombre}” volverá al catálogo.`,
      textoConfirmar: baja ? 'Dar de baja' : 'Reactivar',
      peligro: baja,
    }).subscribe((ok) => {
      if (!ok) return;
      this.api.cambiarEstado(producto.id, !producto.activo).subscribe({
        next: () => this.cargar(),
        error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar'),
      });
    });
  }
}
