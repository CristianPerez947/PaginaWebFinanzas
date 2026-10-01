import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { FechaHoraPipe, MonedaPipe } from '../../../compartido/infraestructura/formato';
import { ETIQUETA_MODALIDAD, Venta } from '../../dominio/modelo/credito.entidad';

/**
 * Historial de compras fiadas: cada compra con sus productos, modalidad y total.
 */
@Component({
  selector: 'app-lista-compras',
  imports: [MatIconModule, MonedaPipe, FechaHoraPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (v of ventas(); track v.id) {
      <article class="tarjeta compra">
        <header>
          <strong><mat-icon>shopping_cart</mat-icon> Compra #{{ v.id }}</strong>
          <span class="texto-suave">{{ v.fechaHora | fechaHora }}</span>
          <span class="espaciador"></span>
          <strong class="total">{{ v.total | moneda: v.moneda }}</strong>
        </header>
        <table class="tabla">
          <thead>
            <tr><th>Producto</th><th class="num">Cantidad</th><th class="num">Precio</th><th>Pago</th><th class="num">Subtotal</th></tr>
          </thead>
          <tbody>
            @for (l of v.lineas; track $index) {
              <tr>
                <td>{{ l.nombreProducto }}</td>
                <td class="num">{{ l.cantidad }}</td>
                <td class="num">{{ l.precioUnitario | moneda: v.moneda }}</td>
                <td>
                  {{ etiqueta[l.modalidad] }}
                  @if (l.numCuotas) { ({{ l.numCuotas }}) }
                </td>
                <td class="num">{{ l.subtotal | moneda: v.moneda }}</td>
              </tr>
            }
          </tbody>
        </table>
      </article>
    } @empty {
      <div class="vacio tarjeta"><mat-icon>shopping_basket</mat-icon><p>Aún no hay compras registradas.</p></div>
    }
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    header {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-wrap: wrap;
      margin-bottom: 10px;
    }
    header strong mat-icon {
      vertical-align: middle;
      margin-right: 4px;
    }
    .total {
      font-size: 1.15rem;
    }
  `,
})
export class ListaCompras {
  readonly ventas = input.required<Venta[]>();
  protected readonly etiqueta = ETIQUETA_MODALIDAD;
}
