import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FechaHoraPipe, FechaPipe, MonedaPipe, TasaPipe } from '../../../compartido/infraestructura/formato';
import { Cuota, ETIQUETA_MODALIDAD, Obligacion } from '../../dominio/modelo/credito.entidad';

/**
 * Detalle de una obligación (deuda de una compra): montos, intereses por días, mora a la fecha del sistema,
 * plan de cuotas y pagos. El vendedor además puede registrar pagos y condonar.
 */
@Component({
  selector: 'app-detalle-obligacion',
  imports: [MatButtonModule, MatIconModule, MonedaPipe, FechaPipe, FechaHoraPipe, TasaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let o = obligacion();
    <section class="obligacion tarjeta" [class.en-mora]="o.enMora">
      <header>
        <div>
          <h3>
            <mat-icon>{{ o.modalidad === 'CUOTAS' ? 'calendar_month' : 'event_available' }}</mat-icon>
            Compra {{ etiquetaModalidad() }}
            @if (o.modalidad === 'CUOTAS') { en {{ o.numCuotas }} cuotas }
          </h3>
          <span class="texto-suave">{{ o.fechaCompra | fechaHora }}</span>
        </div>
        <div class="fila">
          @if (o.enMora) { <span class="etiqueta-estado mora"><mat-icon inline>warning</mat-icon> En mora</span> }
          <span class="etiqueta-estado" [class.pendiente]="o.estado === 'PENDIENTE'" [class.neutra]="o.estado === 'CONDONADA'">
            {{ etiquetaEstado() }}
          </span>
        </div>
      </header>

      <div class="datos">
        <div><small>Capital</small><strong>{{ o.capital | moneda: o.moneda }}</strong></div>
        <div>
          <small>{{ o.modalidad === 'CUOTAS' ? 'Interés capitalizado en la gracia' : 'Interés por los días' }}</small>
          <strong>{{ o.interesCompensatorio | moneda: o.moneda }}</strong>
          <span class="nota">{{ o.diasGracia }} día(s) hasta el {{ o.fechaPago | fecha }}</span>
        </div>
        <div><small>Corte / fecha de pago</small><strong>{{ o.fechaCorte | fecha }} / {{ o.fechaPago | fecha }}</strong></div>
        <div><small>Tasa (TEA)</small><strong>{{ tea() | tasa }}</strong><span class="nota">Mora: {{ teaMora() | tasa }}</span></div>
        @if (o.estado === 'PENDIENTE') {
          <div [class.mora-texto]="o.mora > 0">
            <small>Mora a hoy</small><strong>{{ o.mora | moneda: o.moneda }}</strong>
            @if (o.diasMora > 0) { <span class="nota">{{ o.diasMora }} día(s) de atraso</span> }
          </div>
          <div class="exigible">
            <small>{{ o.modalidad === 'CUOTAS' ? 'Próxima cuota a pagar' : 'Total a pagar' }}</small>
            <strong>{{ o.importeExigible | moneda: o.moneda }}</strong>
          </div>
        }
      </div>

      @if (o.modalidad === 'CUOTAS') {
        <div class="tabla-contenedor">
          <table class="tabla">
            <thead>
              <tr>
                <th>N.°</th><th>Vence</th><th class="num">Cuota</th><th class="num">Interés</th>
                <th class="num">Amortización</th><th class="num">Saldo</th><th>Estado</th>
                @if (vendedor()) { <th></th> }
              </tr>
            </thead>
            <tbody>
              @for (c of o.cuotas; track c.numero) {
                <tr>
                  <td>{{ c.numero }}</td>
                  <td>{{ c.fechaVencimiento | fecha }}</td>
                  <td class="num"><strong>{{ c.cuota | moneda: o.moneda }}</strong></td>
                  <td class="num">{{ c.interes | moneda: o.moneda }}</td>
                  <td class="num">{{ c.amortizacion | moneda: o.moneda }}</td>
                  <td class="num">{{ c.saldo | moneda: o.moneda }}</td>
                  <td>
                    @if (c.estado === 'PAGADA') {
                      <span class="etiqueta-estado">Pagada {{ c.fechaPagoReal | fecha }}</span>
                    } @else if (c.estado === 'CONDONADA') {
                      <span class="etiqueta-estado neutra">Condonada</span>
                    } @else if (c.diasMora > 0) {
                      <span class="etiqueta-estado mora">Mora {{ c.diasMora }} d · {{ c.mora | moneda: o.moneda }}</span>
                    } @else {
                      <span class="etiqueta-estado pendiente">Por vencer</span>
                    }
                  </td>
                  @if (vendedor()) {
                    <td class="num">
                      @if (esSiguiente(c)) {
                        <button mat-flat-button color="primary" (click)="pagarCuota.emit({ obligacion: o, cuota: c })">
                          Registrar pago
                        </button>
                      }
                    </td>
                  }
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else if (vendedor() && o.estado === 'PENDIENTE') {
        <div class="acciones-fila">
          <button mat-flat-button color="primary" (click)="pagarCiclo.emit(o)">
            <mat-icon>payments</mat-icon> Registrar pago del ciclo
          </button>
        </div>
      }

      @if (vendedor() && o.estado === 'PENDIENTE') {
        <div class="acciones-fila">
          <button mat-stroked-button color="warn" (click)="condonar.emit(o)"><mat-icon>volunteer_activism</mat-icon> Condonar esta deuda</button>
        </div>
      }

      @if (o.estado === 'CONDONADA') {
        <div class="aviso-info">
          Condonada el {{ o.fechaCondonacion | fecha }}. Motivo: {{ o.motivoCondonacion }}
        </div>
      }

      @if (o.pagos.length) {
        <details>
          <summary>Pagos registrados ({{ o.pagos.length }})</summary>
          <table class="tabla">
            <thead><tr><th>Fecha</th><th>Concepto</th><th class="num">Mora</th><th class="num">Interés</th><th class="num">Capital</th><th class="num">Total</th></tr></thead>
            <tbody>
              @for (p of o.pagos; track $index) {
                <tr>
                  <td>{{ p.fechaPago | fecha }}</td>
                  <td>{{ p.numeroCuota ? 'Cuota ' + p.numeroCuota : 'Pago total' }}</td>
                  <td class="num">{{ p.montoMora | moneda: o.moneda }}</td>
                  <td class="num">{{ p.montoInteres | moneda: o.moneda }}</td>
                  <td class="num">{{ p.montoCapital | moneda: o.moneda }}</td>
                  <td class="num"><strong>{{ p.total | moneda: o.moneda }}</strong></td>
                </tr>
              }
            </tbody>
          </table>
        </details>
      }
    </section>
  `,
  styles: `
    .obligacion {
      border-left: 5px solid var(--verde);
      display: flex;
      flex-direction: column;
      gap: 14px;
    }
    .obligacion.en-mora {
      border-left-color: var(--rojo);
    }
    header {
      display: flex;
      justify-content: space-between;
      gap: 10px;
      flex-wrap: wrap;
    }
    h3 {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 1.05rem;
    }
    .datos {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
      gap: 12px 18px;
    }
    .datos small {
      display: block;
      color: var(--texto-suave);
      font-size: 0.72rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.03em;
    }
    .datos strong {
      font-size: 1.02rem;
    }
    .nota {
      display: block;
      color: var(--texto-suave);
      font-size: 0.78rem;
    }
    .exigible strong {
      font-size: 1.3rem;
      color: var(--verde);
    }
    .acciones-fila {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
    summary {
      cursor: pointer;
      font-weight: 700;
      margin-bottom: 8px;
    }
  `,
})
export class DetalleObligacion {
  readonly obligacion = input.required<Obligacion>();
  readonly vendedor = input(false);

  readonly pagarCiclo = output<Obligacion>();
  readonly pagarCuota = output<{ obligacion: Obligacion; cuota: Cuota }>();
  readonly condonar = output<Obligacion>();

  protected readonly etiquetaModalidad = computed(() => ETIQUETA_MODALIDAD[this.obligacion().modalidad].toLowerCase());
  protected readonly etiquetaEstado = computed(() => {
    switch (this.obligacion().estado) {
      case 'PENDIENTE':
        return 'Pendiente';
      case 'PAGADA':
        return 'Pagada';
      default:
        return 'Condonada';
    }
  });
  protected readonly tea = computed(() => this.obligacion().teaCompensatoria * 100);
  protected readonly teaMora = computed(() => this.obligacion().teaMora * 100);

  protected esSiguiente(cuota: Cuota): boolean {
    const o = this.obligacion();
    if (o.estado !== 'PENDIENTE') return false;
    return o.cuotas.find((c) => c.estado === 'PENDIENTE')?.numero === cuota.numero;
  }
}
