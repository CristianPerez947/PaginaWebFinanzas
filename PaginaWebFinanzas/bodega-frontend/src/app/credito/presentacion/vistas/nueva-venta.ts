import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { Producto } from '../../../catalogo/dominio/modelo/producto.entidad';
import { CatalogoApi } from '../../../catalogo/infraestructura/catalogo-api';
import { Cliente, nombreCompleto } from '../../../clientes/dominio/modelo/cliente.entidad';
import { ClientesApi } from '../../../clientes/infraestructura/clientes-api';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { MonedaPipe, formatearMoneda } from '../../../compartido/infraestructura/formato';
import { AyudaCampo } from '../../../compartido/presentacion/componentes/ayuda-campo';
import { confirmar } from '../../../compartido/presentacion/componentes/confirmar-dialogo';
import { ImagenFallback } from '../../../compartido/presentacion/componentes/imagen-fallback';
import { DeudaCliente, Modalidad, Venta } from '../../dominio/modelo/credito.entidad';
import { CreditoApi } from '../../infraestructura/credito-api';
import { DetalleObligacion } from '../componentes/detalle-obligacion';

interface LineaEdicion {
  producto: Producto;
  cantidad: number;
  modalidad: Modalidad;
  numCuotas: number | null;
}

/**
 * Registro de una venta fiada: el vendedor elige al cliente y los productos, escoge la forma de pago de cada
 * uno y ve el plan de pagos (fechas, intereses y cuotas) antes de confirmar.
 */
@Component({
  selector: 'app-nueva-venta',
  imports: [
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MonedaPipe,
    AyudaCampo,
    ImagenFallback,
    DetalleObligacion,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Nueva venta fiada</h1>
          <p>Elige al cliente, agrega los productos y revisa el plan de pagos antes de registrar.</p>
        </div>
      </div>
      @if (errorCarga()) { <div class="aviso-error">{{ errorCarga() }}</div> }

      <div class="disposicion">
        <!-- Columna izquierda: cliente y productos -->
        <div class="columna">
          <section class="tarjeta">
            <h2>1. Cliente</h2>
            <mat-form-field>
              <mat-label>Cliente</mat-label>
              <mat-select [value]="clienteId()" (selectionChange)="elegirCliente($event.value)">
                @for (c of clientes(); track c.id) {
                  <mat-option [value]="c.id">{{ nombreDe(c) }} — DNI {{ c.dni }}</mat-option>
                }
              </mat-select>
              <app-ayuda matSuffix texto="Quien recibe el crédito. Solo aparecen clientes activos." />
            </mat-form-field>

            @if (cliente(); as c) {
              <div class="datos-cliente">
                <div><small>Límite</small><strong>{{ c.limiteCredito | moneda: c.moneda }}</strong></div>
                <div><small>Disponible</small><strong [class.mora-texto]="(deuda()?.disponible ?? 0) <= 0">{{ (deuda()?.disponible ?? c.limiteCredito) | moneda: c.moneda }}</strong></div>
                <div><small>Máx. meses</small><strong>{{ c.maxMeses }}</strong></div>
                <div><small>Corte / Pago</small><strong>Día {{ c.diaCorte }} / día {{ c.diaPago }}</strong></div>
              </div>
              @if (deuda()?.enMora) {
                <div class="aviso-error">Este cliente tiene deuda vencida. No se le puede dar crédito nuevo hasta que regularice su pago.</div>
              }
              @if (c.moneda === 'USD') {
                <div class="aviso-info">El crédito de este cliente es en dólares: los precios se convertirán con el tipo de cambio de la tienda.</div>
              }
            }
          </section>

          <section class="tarjeta">
            <h2>2. Productos</h2>
            <mat-form-field>
              <mat-label>Buscar producto</mat-label>
              <mat-icon matPrefix>search</mat-icon>
              <input matInput [ngModel]="busqueda()" (ngModelChange)="busqueda.set($event)" />
            </mat-form-field>
            <div class="lista-productos">
              @for (p of productosFiltrados(); track p.id) {
                <button type="button" class="prod" (click)="agregar(p)" [disabled]="!cliente()">
                  <div class="mini"><app-imagen [src]="p.imagen ? 'assets/productos/' + p.imagen : null" [alt]="p.nombre" /></div>
                  <span class="nombre">{{ p.nombre }}</span>
                  <span class="precio">{{ p.precioCredito | moneda }}</span>
                  <mat-icon>add_circle</mat-icon>
                </button>
              } @empty {
                <p class="texto-suave">No hay productos que coincidan.</p>
              }
            </div>
          </section>
        </div>

        <!-- Columna derecha: líneas, totales y plan -->
        <div class="columna">
          <section class="tarjeta">
            <h2>3. Detalle de la venta</h2>
            @if (lineas().length === 0) {
              <div class="vacio"><mat-icon>shopping_cart</mat-icon><p>Agrega productos desde la lista.</p></div>
            } @else {
              <div class="lineas">
                @for (l of lineas(); track l.producto.id; let i = $index) {
                  <div class="linea">
                    <div class="nombre-linea">
                      <strong>{{ l.producto.nombre }}</strong>
                      <span class="texto-suave">{{ precioUnitario(l) | moneda }} por {{ l.producto.unidadMedida }}</span>
                    </div>
                    <mat-form-field class="cantidad">
                      <mat-label>Cantidad</mat-label>
                      <input matInput type="number" min="0.001" step="any" [ngModel]="l.cantidad" (ngModelChange)="cambiarCantidad(i, $event)" />
                    </mat-form-field>
                    <mat-form-field class="modalidad">
                      <mat-label>Forma de pago</mat-label>
                      <mat-select [value]="l.modalidad" (selectionChange)="cambiarModalidad(i, $event.value)">
                        <mat-option value="FIN_DE_MES">A fin de mes</mat-option>
                        @if (l.producto.permiteCuotas && (cliente()?.maxMeses ?? 0) >= 2) { <mat-option value="CUOTAS">En cuotas</mat-option> }
                        <mat-option value="CONTADO">Al contado</mat-option>
                      </mat-select>
                    </mat-form-field>
                    @if (l.modalidad === 'CUOTAS') {
                      <mat-form-field class="cuotas">
                        <mat-label>Cuotas</mat-label>
                        <mat-select [value]="l.numCuotas" (selectionChange)="cambiarCuotas(i, $event.value)">
                          @for (n of opcionesCuotas(); track n) { <mat-option [value]="n">{{ n }} meses</mat-option> }
                        </mat-select>
                      </mat-form-field>
                    }
                    <strong class="subtotal">{{ subtotal(l) | moneda }}</strong>
                    <button mat-icon-button (click)="quitar(i)" aria-label="Quitar producto"><mat-icon>delete</mat-icon></button>
                  </div>
                }
              </div>

              <div class="totales">
                <div><span>A crédito (estimado)</span><strong>{{ totalCredito() | moneda }}</strong></div>
                <div><span>Al contado</span><strong>{{ totalContado() | moneda }}</strong></div>
              </div>
              <p class="texto-suave nota">
                Estimado en soles con precios de lista. El sistema calcula los importes exactos (y la conversión a dólares
                si corresponde) al calcular el plan.
              </p>
            }

            @if (error()) { <div class="aviso-error">{{ error() }}</div> }

            <div class="fila acciones-venta">
              <button mat-stroked-button (click)="calcular()" [disabled]="!puedeCalcular() || calculando()">
                <mat-icon>calculate</mat-icon> {{ calculando() ? 'Calculando…' : 'Calcular plan de pagos' }}
              </button>
              <button mat-flat-button color="primary" (click)="registrar()" [disabled]="!puedeCalcular() || registrando()">
                <mat-icon>check_circle</mat-icon> Registrar venta
              </button>
            </div>
          </section>

          @if (previa(); as v) {
            <section class="tarjeta">
              <h2>Plan de pagos</h2>
              <div class="aviso-ok">
                Total de la venta: {{ v.total | moneda: v.moneda }}
                @if (v.tipoCambioAplicado) { (tipo de cambio {{ v.tipoCambioAplicado }}) }
              </div>
              <div class="planes">
                @for (o of v.obligaciones ?? []; track $index) { <app-detalle-obligacion [obligacion]="o" /> }
                @if (!(v.obligaciones ?? []).length) {
                  <p class="texto-suave">Todas las líneas son al contado: no se genera deuda.</p>
                }
              </div>
            </section>
          }
        </div>
      </div>
    </div>
  `,
  styles: `
    .disposicion {
      display: grid;
      grid-template-columns: minmax(300px, 4fr) minmax(0, 7fr);
      gap: 16px;
      align-items: start;
    }
    @media (max-width: 980px) {
      .disposicion {
        grid-template-columns: 1fr;
      }
    }
    .columna {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 0;
    }
    .datos-cliente {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 10px;
      margin-top: 4px;
    }
    small {
      display: block;
      color: var(--texto-suave);
      font-size: 0.72rem;
      font-weight: 700;
      text-transform: uppercase;
    }
    .lista-productos {
      max-height: 420px;
      overflow: auto;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .prod {
      display: flex;
      align-items: center;
      gap: 10px;
      text-align: left;
      border: 1px solid var(--borde);
      background: #fff;
      border-radius: 12px;
      padding: 6px 10px 6px 6px;
      cursor: pointer;
      font: inherit;
      color: inherit;
    }
    .prod:hover:not(:disabled) {
      border-color: var(--verde);
      background: var(--verde-suave);
    }
    .prod:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .prod .mini {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      overflow: hidden;
      flex: none;
    }
    .prod .nombre {
      flex: 1;
      font-weight: 700;
      line-height: 1.2;
    }
    .prod .precio {
      color: var(--ambar);
      font-weight: 800;
    }
    .prod mat-icon {
      color: var(--verde);
    }
    .lineas {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .linea {
      display: grid;
      grid-template-columns: minmax(150px, 1fr) 110px 170px auto auto auto;
      gap: 10px;
      align-items: center;
      border-bottom: 1px solid #efede4;
      padding-bottom: 10px;
    }
    .linea:has(.cuotas) {
      grid-template-columns: minmax(150px, 1fr) 110px 170px 130px auto auto;
    }
    @media (max-width: 760px) {
      .linea,
      .linea:has(.cuotas) {
        grid-template-columns: 1fr 1fr;
      }
      .nombre-linea {
        grid-column: 1 / -1;
      }
    }
    .nombre-linea span {
      display: block;
      font-size: 0.82rem;
    }
    .subtotal {
      min-width: 88px;
      text-align: right;
    }
    .totales {
      display: flex;
      flex-direction: column;
      gap: 4px;
      align-items: flex-end;
      margin-top: 12px;
    }
    .totales div {
      display: flex;
      gap: 24px;
      justify-content: space-between;
      min-width: 260px;
    }
    .totales strong {
      font-size: 1.1rem;
    }
    .nota {
      font-size: 0.82rem;
      margin: 8px 0 0;
    }
    .acciones-venta {
      margin-top: 14px;
      justify-content: flex-end;
    }
    .planes {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }
  `,
})
export class NuevaVenta {
  /** Cliente preseleccionado (parámetro ?clienteId=). */
  readonly clienteIdParam = input<string | undefined>(undefined, { alias: 'clienteId' });

  private readonly clientesApi = inject(ClientesApi);
  private readonly catalogoApi = inject(CatalogoApi);
  private readonly creditoApi = inject(CreditoApi);
  private readonly dialogo = inject(MatDialog);
  private readonly avisos = inject(MatSnackBar);
  private readonly router = inject(Router);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly clienteId = signal<number | null>(null);
  protected readonly deuda = signal<DeudaCliente | null>(null);
  protected readonly lineas = signal<LineaEdicion[]>([]);
  protected readonly busqueda = signal('');
  protected readonly previa = signal<Venta | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly errorCarga = signal<string | null>(null);
  protected readonly calculando = signal(false);
  protected readonly registrando = signal(false);

  protected readonly cliente = computed(() => this.clientes().find((c) => c.id === this.clienteId()) ?? null);

  protected readonly opcionesCuotas = computed(() => {
    const max = this.cliente()?.maxMeses ?? 0;
    return Array.from({ length: Math.max(0, max - 1) }, (_, i) => i + 2);
  });

  protected readonly productosFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.productos().filter((p) => !texto || `${p.nombre} ${p.marca ?? ''} ${p.categoria ?? ''}`.toLowerCase().includes(texto));
  });

  protected readonly totalCredito = computed(() =>
    this.lineas().filter((l) => l.modalidad !== 'CONTADO').reduce((s, l) => s + this.subtotal(l), 0),
  );
  protected readonly totalContado = computed(() =>
    this.lineas().filter((l) => l.modalidad === 'CONTADO').reduce((s, l) => s + this.subtotal(l), 0),
  );

  protected readonly puedeCalcular = computed(
    () =>
      this.cliente() !== null &&
      this.lineas().length > 0 &&
      this.lineas().every((l) => l.cantidad > 0 && (l.modalidad !== 'CUOTAS' || (l.numCuotas ?? 0) >= 2)),
  );

  constructor() {
    forkJoin({ clientes: this.clientesApi.listar(), productos: this.catalogoApi.listar() }).subscribe({
      next: (r) => {
        this.clientes.set(r.clientes.filter((c) => c.activo));
        this.productos.set(r.productos.filter((p) => p.activo));
        const inicial = Number(this.clienteIdParam());
        if (inicial && this.clientes().some((c) => c.id === inicial)) this.elegirCliente(inicial);
      },
      error: (e) => this.errorCarga.set(mensajeDeError(e)),
    });
    effect(() => {
      const id = Number(this.clienteIdParam());
      if (id && this.clientes().some((c) => c.id === id) && this.clienteId() !== id) this.elegirCliente(id);
    });
  }

  protected nombreDe(c: Cliente): string {
    return nombreCompleto(c);
  }

  protected elegirCliente(id: number): void {
    this.clienteId.set(id);
    this.deuda.set(null);
    this.previa.set(null);
    this.error.set(null);
    this.creditoApi.deudaDeCliente(id).subscribe({ next: (d) => this.deuda.set(d) });
    // Las cuotas elegidas deben respetar el máximo del nuevo cliente
    const max = this.clientes().find((c) => c.id === id)?.maxMeses ?? 0;
    this.lineas.update((ls) =>
      ls.map((l) =>
        l.modalidad === 'CUOTAS' && (l.numCuotas ?? 0) > max ? { ...l, modalidad: 'FIN_DE_MES', numCuotas: null } : l,
      ),
    );
  }

  protected precioUnitario(l: LineaEdicion): number {
    return l.modalidad === 'CONTADO' ? l.producto.precioContado : l.producto.precioCredito;
  }

  protected subtotal(l: LineaEdicion): number {
    return Math.round(l.cantidad * this.precioUnitario(l) * 100) / 100;
  }

  protected agregar(producto: Producto): void {
    this.previa.set(null);
    this.lineas.update((ls) => {
      const existente = ls.findIndex((l) => l.producto.id === producto.id);
      if (existente >= 0) return ls.map((l, i) => (i === existente ? { ...l, cantidad: l.cantidad + 1 } : l));
      return [...ls, { producto, cantidad: 1, modalidad: 'FIN_DE_MES', numCuotas: null }];
    });
  }

  protected cambiarCantidad(indice: number, valor: number | string): void {
    this.previa.set(null);
    const cantidad = Number(valor);
    this.lineas.update((ls) => ls.map((l, i) => (i === indice ? { ...l, cantidad: isNaN(cantidad) ? 0 : cantidad } : l)));
  }

  protected cambiarModalidad(indice: number, modalidad: Modalidad): void {
    this.previa.set(null);
    this.lineas.update((ls) =>
      ls.map((l, i) =>
        i === indice ? { ...l, modalidad, numCuotas: modalidad === 'CUOTAS' ? (this.opcionesCuotas()[0] ?? 2) : null } : l,
      ),
    );
  }

  protected cambiarCuotas(indice: number, cuotas: number): void {
    this.previa.set(null);
    this.lineas.update((ls) => ls.map((l, i) => (i === indice ? { ...l, numCuotas: cuotas } : l)));
  }

  protected quitar(indice: number): void {
    this.previa.set(null);
    this.lineas.update((ls) => ls.filter((_, i) => i !== indice));
  }

  private solicitud() {
    return {
      clienteId: this.clienteId()!,
      lineas: this.lineas().map((l) => ({
        productoId: l.producto.id,
        cantidad: l.cantidad,
        modalidad: l.modalidad,
        numCuotas: l.modalidad === 'CUOTAS' ? l.numCuotas : null,
      })),
    };
  }

  protected calcular(): void {
    this.calculando.set(true);
    this.error.set(null);
    this.creditoApi.simularVenta(this.solicitud()).subscribe({
      next: (v) => {
        this.previa.set(v);
        this.calculando.set(false);
      },
      error: (e) => {
        this.previa.set(null);
        this.error.set(mensajeDeError(e));
        this.calculando.set(false);
      },
    });
  }

  protected registrar(): void {
    const c = this.cliente();
    if (!c) return;
    confirmar(this.dialogo, {
      titulo: 'Registrar la venta',
      mensaje: `Se registrará una venta fiada para ${nombreCompleto(c)} por unos ${formatearMoneda(this.totalCredito() + this.totalContado())}. Las ventas no se pueden anular.`,
      textoConfirmar: 'Registrar venta',
    }).subscribe((ok) => {
      if (!ok) return;
      this.registrando.set(true);
      this.error.set(null);
      this.creditoApi.registrarVenta(this.solicitud()).subscribe({
        next: (v) => {
          this.avisos.open(`Venta registrada por ${formatearMoneda(v.total, v.moneda)}`, 'Cerrar');
          this.router.navigate(['/vendedor/clientes', c.id]);
        },
        error: (e) => {
          this.error.set(mensajeDeError(e));
          this.registrando.set(false);
        },
      });
    });
  }
}
