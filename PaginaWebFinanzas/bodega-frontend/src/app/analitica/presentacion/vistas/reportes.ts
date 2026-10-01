import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { forkJoin } from 'rxjs';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { MonedaPipe } from '../../../compartido/infraestructura/formato';
import { Grafico } from '../../../compartido/presentacion/componentes/grafico';
import {
  FilaDeudor,
  FilaInteresMensual,
  FilaMoroso,
  FilaProductoVendido,
  ResumenCartera,
} from '../../dominio/modelo/analitica.entidad';
import { AnaliticaApi } from '../../infraestructura/analitica-api';

const ETIQUETAS: Record<string, string> = {
  CONTADO: 'Al contado',
  FIN_DE_MES: 'A fin de mes',
  CUOTAS: 'En cuotas',
  PENDIENTE: 'Pendiente',
  PAGADA: 'Pagada',
  CONDONADA: 'Condonada',
};

/**
 * Reportes del vendedor: deudores, morosos, ventas por producto, intereses cobrados y cartera.
 */
@Component({
  selector: 'app-reportes',
  imports: [RouterLink, MatIconModule, MatTabsModule, MonedaPipe, Grafico],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Reportes</h1>
          <p>Información para decidir a quién cobrar, qué vender y cuánto ganas en intereses. Importes en soles.</p>
        </div>
      </div>
      @if (error()) { <div class="aviso-error">{{ error() }}</div> }

      <mat-tab-group animationDuration="150ms">
        <mat-tab label="Deudores">
          <div class="contenido">
            <div class="tabla-contenedor">
              <table class="tabla">
                <thead><tr><th>Cliente</th><th>Teléfono</th><th class="num">Capital pendiente</th><th class="num">Límite</th><th class="num">Exigible hoy</th><th>Estado</th></tr></thead>
                <tbody>
                  @for (d of deudores(); track d.clienteId) {
                    <tr>
                      <td><a class="enlace" [routerLink]="['/vendedor/clientes', d.clienteId]">{{ d.cliente }}</a><div class="texto-suave">DNI {{ d.dni }}</div></td>
                      <td>{{ d.telefono }}</td>
                      <td class="num"><strong>{{ d.capitalPendiente | moneda }}</strong></td>
                      <td class="num">{{ d.limiteCredito | moneda }}</td>
                      <td class="num">{{ d.importeExigible | moneda }}</td>
                      <td><span class="etiqueta-estado" [class.mora]="d.enMora">{{ d.enMora ? 'En mora' : 'Al día' }}</span></td>
                    </tr>
                  } @empty { <tr><td colspan="6" class="vacio">Sin deudores.</td></tr> }
                </tbody>
              </table>
            </div>
          </div>
        </mat-tab>

        <mat-tab label="Morosos">
          <div class="contenido">
            <div class="tabla-contenedor">
              <table class="tabla">
                <thead><tr><th>Cliente</th><th>Teléfono</th><th class="num">Días de atraso</th><th class="num">Importe vencido</th><th class="num">Mora acumulada</th></tr></thead>
                <tbody>
                  @for (m of morosos(); track m.clienteId) {
                    <tr>
                      <td><a class="enlace" [routerLink]="['/vendedor/clientes', m.clienteId]">{{ m.cliente }}</a></td>
                      <td>{{ m.telefono }}</td>
                      <td class="num mora-texto">{{ m.diasMoraMaximo }}</td>
                      <td class="num"><strong>{{ m.importeVencido | moneda }}</strong></td>
                      <td class="num mora-texto">{{ m.moraAcumulada | moneda }}</td>
                    </tr>
                  } @empty { <tr><td colspan="5" class="vacio">No hay clientes en mora.</td></tr> }
                </tbody>
              </table>
            </div>
          </div>
        </mat-tab>

        <mat-tab label="Ventas por producto">
          <div class="contenido">
            <section class="tarjeta">
              <app-grafico tipo="bar" [horizontal]="true" [etiquetas]="nombresProductos()" [series]="[{ nombre: 'Ventas', valores: montosProductos(), color: '#d98a1c' }]" descripcion="Ventas por producto" />
            </section>
            <div class="tabla-contenedor">
              <table class="tabla">
                <thead><tr><th>Producto</th><th class="num">Cantidad vendida</th><th class="num">Monto</th></tr></thead>
                <tbody>
                  @for (p of productos(); track p.producto) {
                    <tr><td>{{ p.producto }}</td><td class="num">{{ p.cantidad }}</td><td class="num"><strong>{{ p.monto | moneda }}</strong></td></tr>
                  } @empty { <tr><td colspan="3" class="vacio">Sin ventas.</td></tr> }
                </tbody>
              </table>
            </div>
          </div>
        </mat-tab>

        <mat-tab label="Intereses ganados">
          <div class="contenido">
            <section class="tarjeta">
              <app-grafico tipo="bar" [apilado]="true" [etiquetas]="mesesIntereses()"
                [series]="[{ nombre: 'Compensatorios', valores: compensatorios(), color: '#1f6f4a' }, { nombre: 'Mora', valores: moras(), color: '#b3261e' }]"
                descripcion="Intereses cobrados por mes" />
            </section>
            <div class="tabla-contenedor">
              <table class="tabla">
                <thead><tr><th>Mes</th><th class="num">Intereses compensatorios</th><th class="num">Intereses por mora</th><th class="num">Total</th></tr></thead>
                <tbody>
                  @for (i of intereses(); track i.mes) {
                    <tr><td>{{ i.mes }}</td><td class="num">{{ i.interesesCompensatorios | moneda }}</td><td class="num">{{ i.interesesMora | moneda }}</td><td class="num"><strong>{{ i.total | moneda }}</strong></td></tr>
                  } @empty { <tr><td colspan="4" class="vacio">Aún no se han cobrado intereses.</td></tr> }
                </tbody>
              </table>
            </div>
          </div>
        </mat-tab>

        <mat-tab label="Cartera">
          <div class="contenido">
            @if (cartera(); as c) {
              <div class="cuadricula kpis">
                <div class="kpi"><span class="etiqueta">Capital pendiente</span><span class="valor">{{ c.capitalPendiente | moneda }}</span></div>
                <div class="kpi" [class.alerta]="c.deudaVencida > 0"><span class="etiqueta">Deuda vencida</span><span class="valor">{{ c.deudaVencida | moneda }}</span></div>
                <div class="kpi"><span class="etiqueta">Intereses por cobrar</span><span class="valor">{{ c.interesesPorCobrar | moneda }}</span></div>
                <div class="kpi bien"><span class="etiqueta">Intereses cobrados</span><span class="valor">{{ c.interesesCobrados | moneda }}</span></div>
                <div class="kpi"><span class="etiqueta">Mora cobrada</span><span class="valor">{{ c.moraCobrada | moneda }}</span></div>
                <div class="kpi bien"><span class="etiqueta">Total cobrado</span><span class="valor">{{ c.totalCobrado | moneda }}</span></div>
              </div>
              <div class="cuadricula dos">
                <section class="tarjeta"><h2>Ventas por forma de pago</h2><app-grafico tipo="doughnut" [etiquetas]="etiquetas(c.porModalidad)" [series]="[{ nombre: 'Ventas', valores: valores(c.porModalidad) }]" /></section>
                <section class="tarjeta"><h2>Deuda registrada por estado (capital)</h2><app-grafico tipo="doughnut" [etiquetas]="etiquetas(c.porEstado)" [series]="[{ nombre: 'Capital', valores: valores(c.porEstado) }]" /></section>
              </div>
            }
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: `
    .contenido {
      padding: 16px 0;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .enlace {
      color: var(--verde);
      font-weight: 700;
      text-decoration: none;
    }
  `,
})
export class Reportes {
  private readonly api = inject(AnaliticaApi);

  protected readonly deudores = signal<FilaDeudor[]>([]);
  protected readonly morosos = signal<FilaMoroso[]>([]);
  protected readonly productos = signal<FilaProductoVendido[]>([]);
  protected readonly intereses = signal<FilaInteresMensual[]>([]);
  protected readonly cartera = signal<ResumenCartera | null>(null);
  protected readonly error = signal<string | null>(null);

  protected nombresProductos = () => this.productos().slice(0, 12).map((p) => p.producto);
  protected montosProductos = () => this.productos().slice(0, 12).map((p) => p.monto);
  protected mesesIntereses = () => this.intereses().map((i) => i.mes);
  protected compensatorios = () => this.intereses().map((i) => i.interesesCompensatorios);
  protected moras = () => this.intereses().map((i) => i.interesesMora);
  protected etiquetas = (serie: { etiqueta: string }[]) => serie.map((p) => ETIQUETAS[p.etiqueta] ?? p.etiqueta);
  protected valores = (serie: { valor: number }[]) => serie.map((p) => p.valor);

  constructor() {
    forkJoin({
      deudores: this.api.deudores(),
      morosos: this.api.morosos(),
      productos: this.api.ventasPorProducto(),
      intereses: this.api.intereses(),
      cartera: this.api.cartera(),
    }).subscribe({
      next: (r) => {
        this.deudores.set(r.deudores);
        this.morosos.set(r.morosos);
        this.productos.set(r.productos);
        this.intereses.set(r.intereses);
        this.cartera.set(r.cartera);
      },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
