import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { FechaPipe, MonedaPipe } from '../../../compartido/infraestructura/formato';
import { EstadoCuenta } from '../../dominio/modelo/estado-cuenta.entidad';

export interface DatosEstadoCuenta {
  estado: EstadoCuenta;
  descargar: () => void;
}

/**
 * Detalle de un estado de cuenta: compras ordenadas por fecha, intereses por los días transcurridos y, en un
 * ítem aparte, los intereses por mora.
 */
@Component({
  selector: 'app-estado-cuenta-dialogo',
  imports: [MatButtonModule, MatDialogModule, MatIconModule, MonedaPipe, FechaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let e = datos.estado;
    <h2 mat-dialog-title>Estado de cuenta — corte {{ e.fechaCorte | fecha }}</h2>
    <mat-dialog-content>
      <div class="cabecera">
        <div><small>Cliente</small><strong>{{ e.nombreCliente }}</strong></div>
        <div><small>Fecha de pago</small><strong>{{ e.fechaPago | fecha }}</strong></div>
        <div>
          <small>Estado</small>
          <span class="etiqueta-estado" [class.pendiente]="e.estado === 'PENDIENTE'">{{ e.estado === 'PAGADO' ? 'Pagado' : 'Pendiente de pago' }}</span>
        </div>
      </div>

      <div class="tabla-contenedor">
        <table class="tabla">
          <thead><tr><th>Fecha</th><th>Detalle</th><th class="num">Monto</th></tr></thead>
          <tbody>
            @for (l of e.lineas; track $index) {
              <tr [class.mora]="l.tipo === 'MORA'" [class.info]="!l.incluidaEnTotal">
                <td>{{ l.fecha | fecha }}</td>
                <td>{{ l.descripcion }}</td>
                <td class="num">
                  @if (l.incluidaEnTotal) { {{ l.monto | moneda: e.moneda }} }
                  @else { <span class="texto-suave">({{ l.monto | moneda: e.moneda }} informativo)</span> }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      <div class="totales">
        <div><span>Compras a fin de mes</span><span>{{ e.totalCompras | moneda: e.moneda }}</span></div>
        <div><span>Intereses compensatorios</span><span>{{ e.totalIntereses | moneda: e.moneda }}</span></div>
        <div><span>Cuotas del plan de pagos</span><span>{{ e.totalCuotas | moneda: e.moneda }}</span></div>
        <div [class.mora-texto]="e.totalMora > 0"><span>Intereses por mora</span><span>{{ e.totalMora | moneda: e.moneda }}</span></div>
        <div class="total"><span>TOTAL A PAGAR</span><span>{{ e.totalAPagar | moneda: e.moneda }}</span></div>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cerrar</button>
      <button mat-flat-button color="primary" (click)="datos.descargar()"><mat-icon>picture_as_pdf</mat-icon> Descargar PDF</button>
    </mat-dialog-actions>
  `,
  styles: `
    .cabecera {
      display: flex;
      gap: 28px;
      flex-wrap: wrap;
      margin-bottom: 14px;
    }
    small {
      display: block;
      color: var(--texto-suave);
      font-size: 0.72rem;
      font-weight: 700;
      text-transform: uppercase;
    }
    tr.mora td {
      color: var(--rojo);
      font-weight: 700;
    }
    tr.info td {
      color: var(--texto-suave);
      font-style: italic;
    }
    .totales {
      margin: 14px 0 0 auto;
      max-width: 340px;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .totales div {
      display: flex;
      justify-content: space-between;
      gap: 16px;
    }
    .totales .total {
      border-top: 2px solid var(--texto);
      padding-top: 8px;
      margin-top: 4px;
      font-size: 1.15rem;
      font-weight: 800;
    }
  `,
})
export class EstadoCuentaDialogo {
  protected readonly datos = inject<DatosEstadoCuenta>(MAT_DIALOG_DATA);
}
