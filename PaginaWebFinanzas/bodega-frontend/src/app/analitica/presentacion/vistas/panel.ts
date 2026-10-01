import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { FechaPipe, MonedaPipe } from '../../../compartido/infraestructura/formato';
import { Grafico } from '../../../compartido/presentacion/componentes/grafico';
import { PanelVendedor } from '../../dominio/modelo/analitica.entidad';
import { AnaliticaApi } from '../../infraestructura/analitica-api';

const ETIQUETA_MODALIDAD: Record<string, string> = { CONTADO: 'Al contado', FIN_DE_MES: 'A fin de mes', CUOTAS: 'En cuotas' };

function nombreMes(iso: string): string {
  const [anio, mes] = iso.split('-');
  const nombres = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];
  return `${nombres[Number(mes) - 1]} ${anio.substring(2)}`;
}

/**
 * Panel del vendedor: cartera, deuda vencida, cobros, ventas, morosos y próximos vencimientos.
 */
@Component({
  selector: 'app-panel',
  imports: [RouterLink, MatButtonModule, MatIconModule, MonedaPipe, FechaPipe, Grafico],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Panel de control</h1>
          <p>Resumen de tu cuaderno de fiado al {{ panel()?.fecha | fecha }} (importes en soles).</p>
        </div>
        <div class="fila">
          <a mat-flat-button color="primary" routerLink="/vendedor/ventas/nueva"><mat-icon>point_of_sale</mat-icon> Nueva venta</a>
          <a mat-stroked-button routerLink="/vendedor/cobranzas"><mat-icon>request_quote</mat-icon> Cobranzas</a>
        </div>
      </div>

      @if (error()) { <div class="aviso-error">{{ error() }}</div> }

      @if (panel(); as p) {
        <div class="cuadricula kpis">
          <div class="kpi"><span class="etiqueta">Cartera pendiente</span><span class="valor">{{ p.carteraPendiente | moneda }}</span><span class="nota">{{ p.clientesConDeuda }} cliente(s) con deuda</span></div>
          <div class="kpi" [class.alerta]="p.deudaVencida > 0"><span class="etiqueta">Deuda vencida</span><span class="valor">{{ p.deudaVencida | moneda }}</span><span class="nota">Con mora incluida</span></div>
          <div class="kpi" [class.alerta]="p.clientesEnMora > 0"><span class="etiqueta">Clientes en mora</span><span class="valor">{{ p.clientesEnMora }}</span><span class="nota">de {{ p.clientesActivos }} activos</span></div>
          <div class="kpi bien"><span class="etiqueta">Total cobrado</span><span class="valor">{{ p.totalCobrado | moneda }}</span><span class="nota">Pagos registrados</span></div>
          <div class="kpi"><span class="etiqueta">Intereses por cobrar</span><span class="valor">{{ p.interesesPorCobrar | moneda }}</span><span class="nota">De deudas pendientes</span></div>
          <div class="kpi"><span class="etiqueta">Ventas del mes</span><span class="valor">{{ p.ventasDelMes | moneda }}</span><span class="nota">Contado y crédito</span></div>
        </div>

        <div class="cuadricula dos graficos">
          <section class="tarjeta">
            <h2>Ventas por mes</h2>
            <app-grafico tipo="bar" [etiquetas]="mesesVentas()" [series]="[{ nombre: 'Ventas', valores: valoresVentas() }]" descripcion="Ventas por mes" />
          </section>
          <section class="tarjeta">
            <h2>Cobros por mes</h2>
            <app-grafico tipo="line" [etiquetas]="mesesCobros()" [series]="[{ nombre: 'Cobrado', valores: valoresCobros(), color: '#2f6fb0' }]" descripcion="Cobros por mes" />
          </section>
          <section class="tarjeta">
            <h2>Ventas por forma de pago</h2>
            <app-grafico tipo="doughnut" [etiquetas]="etiquetasModalidad()" [series]="[{ nombre: 'Ventas', valores: valoresModalidad() }]" descripcion="Ventas por forma de pago" />
          </section>
          <section class="tarjeta">
            <h2>Productos más vendidos</h2>
            <app-grafico tipo="bar" [horizontal]="true" [etiquetas]="nombresProductos()" [series]="[{ nombre: 'Ventas', valores: montosProductos(), color: '#d98a1c' }]" descripcion="Productos más vendidos" />
          </section>
        </div>

        <div class="cuadricula dos">
          <section class="tarjeta">
            <h2><mat-icon inline>warning</mat-icon> Clientes en mora</h2>
            @for (m of p.morosos; track m.clienteId) {
              <a class="item" [routerLink]="['/vendedor/clientes', m.clienteId]">
                <div><strong>{{ m.cliente }}</strong><span class="texto-suave">{{ m.telefono }} · {{ m.diasMoraMaximo }} día(s) de atraso</span></div>
                <div class="num"><strong class="mora-texto">{{ m.importeVencido | moneda }}</strong><span class="texto-suave">mora {{ m.moraAcumulada | moneda }}</span></div>
              </a>
            } @empty {
              <p class="texto-suave">Ningún cliente en mora. ¡Buen trabajo!</p>
            }
          </section>
          <section class="tarjeta">
            <h2><mat-icon inline>event</mat-icon> Próximos vencimientos (15 días)</h2>
            @for (v of p.proximosVencimientos; track $index) {
              <a class="item" [routerLink]="['/vendedor/clientes', v.clienteId]">
                <div><strong>{{ v.cliente }}</strong><span class="texto-suave">{{ v.concepto }} · {{ v.fecha | fecha }}</span></div>
                <div class="num"><strong>{{ v.importe | moneda }}</strong></div>
              </a>
            } @empty {
              <p class="texto-suave">No hay vencimientos en los próximos 15 días.</p>
            }
          </section>
          <section class="tarjeta">
            <h2>Mayores deudores</h2>
            @for (d of p.topDeudores; track d.clienteId) {
              <a class="item" [routerLink]="['/vendedor/clientes', d.clienteId]">
                <div><strong>{{ d.cliente }}</strong><span class="texto-suave">Límite {{ d.limiteCredito | moneda }}</span></div>
                <div class="num"><strong>{{ d.capitalPendiente | moneda }}</strong>@if (d.enMora) { <span class="etiqueta-estado mora">Mora</span> }</div>
              </a>
            } @empty {
              <p class="texto-suave">Sin deudas pendientes.</p>
            }
          </section>
        </div>
      }
    </div>
  `,
  styles: `
    .graficos {
      margin: 16px 0;
    }
    .item {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      padding: 10px 0;
      border-bottom: 1px solid #efede4;
      text-decoration: none;
      color: inherit;
    }
    .item:last-child {
      border-bottom: none;
    }
    .item span {
      display: block;
      font-size: 0.82rem;
    }
    .item:hover strong:first-child {
      color: var(--verde);
    }
    section.tarjeta h2 mat-icon {
      vertical-align: middle;
    }
  `,
})
export class Panel {
  private readonly api = inject(AnaliticaApi);

  protected readonly panel = signal<PanelVendedor | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly mesesVentas = computed(() => this.panel()?.ventasPorMes.map((p) => nombreMes(p.etiqueta)) ?? []);
  protected readonly valoresVentas = computed(() => this.panel()?.ventasPorMes.map((p) => p.valor) ?? []);
  protected readonly mesesCobros = computed(() => this.panel()?.cobrosPorMes.map((p) => nombreMes(p.etiqueta)) ?? []);
  protected readonly valoresCobros = computed(() => this.panel()?.cobrosPorMes.map((p) => p.valor) ?? []);
  protected readonly etiquetasModalidad = computed(() => this.panel()?.ventasPorModalidad.map((p) => ETIQUETA_MODALIDAD[p.etiqueta] ?? p.etiqueta) ?? []);
  protected readonly valoresModalidad = computed(() => this.panel()?.ventasPorModalidad.map((p) => p.valor) ?? []);
  protected readonly nombresProductos = computed(() => this.panel()?.topProductos.map((p) => p.producto) ?? []);
  protected readonly montosProductos = computed(() => this.panel()?.topProductos.map((p) => p.monto) ?? []);

  constructor() {
    this.api.panel().subscribe({
      next: (p) => this.panel.set(p),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
