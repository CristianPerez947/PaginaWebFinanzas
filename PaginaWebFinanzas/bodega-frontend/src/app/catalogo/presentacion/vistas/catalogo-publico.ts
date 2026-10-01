import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { MonedaPipe } from '../../../compartido/infraestructura/formato';
import { ImagenFallback } from '../../../compartido/presentacion/componentes/imagen-fallback';
import { Producto } from '../../dominio/modelo/producto.entidad';
import { CatalogoApi } from '../../infraestructura/catalogo-api';

/**
 * Catálogo público de la bodega: productos con sus precios al contado y a crédito.
 */
@Component({
  selector: 'app-catalogo-publico',
  imports: [FormsModule, MatChipsModule, MatFormFieldModule, MatIconModule, MatInputModule, MonedaPipe, ImagenFallback],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h2>Productos y precios</h2>
          <p>Precios en soles. El precio a crédito es el que se aplica cuando compras fiado.</p>
        </div>
        <mat-form-field class="buscador">
          <mat-label>Buscar producto</mat-label>
          <mat-icon matPrefix>search</mat-icon>
          <input matInput [ngModel]="busqueda()" (ngModelChange)="busqueda.set($event)" placeholder="Arroz, leche, detergente…" />
        </mat-form-field>
      </div>

      @if (error()) {
        <div class="aviso-error">{{ error() }}</div>
      }

      <mat-chip-listbox aria-label="Categorías" class="categorias" [value]="categoria()" (change)="categoria.set($event.value ?? '')">
        <mat-chip-option value="">Todas</mat-chip-option>
        @for (c of categorias(); track c) {
          <mat-chip-option [value]="c">{{ c }}</mat-chip-option>
        }
      </mat-chip-listbox>

      <div class="rejilla">
        @for (p of filtrados(); track p.id) {
          <article class="producto tarjeta">
            <div class="foto"><app-imagen [src]="p.imagen ? 'assets/productos/' + p.imagen : null" [alt]="p.nombre" /></div>
            <div class="cuerpo">
              <h3>{{ p.nombre }}</h3>
              <span class="texto-suave">{{ p.marca }} · por {{ p.unidadMedida }}</span>
              <div class="precios">
                <div>
                  <small>Al contado</small>
                  <strong>{{ p.precioContado | moneda }}</strong>
                </div>
                <div>
                  <small>A crédito</small>
                  <strong class="credito">{{ p.precioCredito | moneda }}</strong>
                </div>
              </div>
              @if (p.permiteCuotas) {
                <span class="etiqueta-estado neutra"><mat-icon inline>calendar_month</mat-icon> Se puede pagar en cuotas</span>
              }
            </div>
          </article>
        } @empty {
          @if (!cargando()) {
            <div class="vacio tarjeta completo"><mat-icon>search_off</mat-icon><p>No hay productos que coincidan.</p></div>
          }
        }
      </div>
    </div>
  `,
  styles: `
    .buscador {
      width: 320px;
      max-width: 100%;
    }
    .categorias {
      margin-bottom: 14px;
    }
    .rejilla {
      display: grid;
      gap: 14px;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    }
    .completo {
      grid-column: 1 / -1;
    }
    .producto {
      padding: 0;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
    .foto {
      height: 150px;
    }
    .cuerpo {
      padding: 12px 14px 14px;
      display: flex;
      flex-direction: column;
      gap: 6px;
      flex: 1;
    }
    .cuerpo h3 {
      font-size: 1rem;
      line-height: 1.25;
    }
    .precios {
      display: flex;
      gap: 18px;
      margin: 4px 0;
    }
    .precios small {
      display: block;
      color: var(--texto-suave);
      font-size: 0.72rem;
      font-weight: 700;
      text-transform: uppercase;
    }
    .precios strong {
      font-size: 1.1rem;
    }
    .credito {
      color: var(--ambar);
    }
  `,
})
export class CatalogoPublico {
  readonly slug = input.required<string>();

  private readonly api = inject(CatalogoApi);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly busqueda = signal('');
  protected readonly categoria = signal('');

  protected readonly categorias = computed(() =>
    [...new Set(this.productos().map((p) => p.categoria).filter((c): c is string => !!c))].sort(),
  );

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.productos().filter(
      (p) =>
        (!this.categoria() || p.categoria === this.categoria()) &&
        (!texto || `${p.nombre} ${p.marca ?? ''}`.toLowerCase().includes(texto)),
    );
  });

  constructor() {
    effect(() => {
      this.cargando.set(true);
      this.api.catalogoPublico(this.slug()).subscribe({
        next: (p) => {
          this.productos.set(p);
          this.cargando.set(false);
        },
        error: (e) => {
          this.error.set(mensajeDeError(e));
          this.cargando.set(false);
        },
      });
    });
  }
}
