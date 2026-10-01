import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FechaPipe, MonedaPipe } from '../../../compartido/infraestructura/formato';
import { EstadoCuenta } from '../../dominio/modelo/estado-cuenta.entidad';

/**
 * Tabla de estados de cuenta con acciones para ver el detalle y descargar el PDF.
 */
@Component({
  selector: 'app-lista-estados-cuenta',
  imports: [RouterLink, MatButtonModule, MatIconModule, MonedaPipe, FechaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="tabla-contenedor">
      <table class="tabla">
        <thead>
          <tr>
            @if (mostrarCliente()) { <th>Cliente</th> }
            <th>Corte</th><th>Fecha de pago</th>
            <th class="num">Intereses</th><th class="num">Mora</th><th class="num">Total a pagar</th>
            <th>Estado</th><th></th>
          </tr>
        </thead>
        <tbody>
          @for (e of estados(); track e.id) {
            <tr>
              @if (mostrarCliente()) {
                <td>
                  <a class="enlace" [routerLink]="['/vendedor/clientes', e.clienteId]">{{ e.nombreCliente }}</a>
                  <div class="texto-suave">DNI {{ e.dniCliente }}</div>
                </td>
              }
              <td>{{ e.fechaCorte | fecha }}</td>
              <td>{{ e.fechaPago | fecha }}</td>
              <td class="num">{{ e.totalIntereses | moneda: e.moneda }}</td>
              <td class="num" [class.mora-texto]="e.totalMora > 0">{{ e.totalMora | moneda: e.moneda }}</td>
              <td class="num"><strong>{{ e.totalAPagar | moneda: e.moneda }}</strong></td>
              <td>
                <span class="etiqueta-estado" [class.pendiente]="e.estado === 'PENDIENTE'">
                  {{ e.estado === 'PAGADO' ? 'Pagado' : 'Pendiente' }}
                </span>
              </td>
              <td>
                <div class="acciones">
                  <button mat-icon-button (click)="ver.emit(e)" title="Ver detalle" aria-label="Ver detalle"><mat-icon>visibility</mat-icon></button>
                  <button mat-icon-button (click)="pdf.emit(e)" title="Descargar PDF" aria-label="Descargar PDF"><mat-icon>picture_as_pdf</mat-icon></button>
                  @if (puedeRegenerar()) {
                    <button mat-icon-button (click)="regenerar.emit(e)" title="Regenerar" aria-label="Regenerar"><mat-icon>refresh</mat-icon></button>
                  }
                </div>
              </td>
            </tr>
          } @empty {
            <tr>
              <td [attr.colspan]="mostrarCliente() ? 8 : 7" class="vacio">
                <mat-icon>description</mat-icon>
                <p>Aún no hay estados de cuenta. Se generan automáticamente al llegar la fecha de corte de cada cliente.</p>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class ListaEstadosCuenta {
  readonly estados = input.required<EstadoCuenta[]>();
  readonly mostrarCliente = input(true);
  readonly puedeRegenerar = input(false);

  readonly ver = output<EstadoCuenta>();
  readonly pdf = output<EstadoCuenta>();
  readonly regenerar = output<EstadoCuenta>();
}
