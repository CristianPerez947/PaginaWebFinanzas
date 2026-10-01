import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { FechaHoraPipe } from '../../../compartido/infraestructura/formato';
import { AyudaCampo } from '../../../compartido/presentacion/componentes/ayuda-campo';
import { RelojSistema } from '../../dominio/modelo/tienda.entidad';
import { TiendasApi } from '../../infraestructura/tiendas-api';

/**
 * Fecha del sistema para la demostración: permite simular una fecha (por ejemplo el día de corte o un día de
 * mora) sin esperar el calendario real. Todos los cálculos del sistema usan esta fecha.
 */
@Component({
  selector: 'app-admin-reloj',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, FechaHoraPipe, AyudaCampo],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Fecha del sistema</h1>
          <p>Modo demostración: simula una fecha para mostrar cortes, mora y vencimientos sin esperar al calendario.</p>
        </div>
      </div>
      @if (error()) { <div class="aviso-error">{{ error() }}</div> }

      <section class="tarjeta actual">
        <mat-icon>{{ reloj()?.simulado ? 'science' : 'schedule' }}</mat-icon>
        <div>
          <small>Fecha y hora que usa el sistema</small>
          <strong>{{ reloj()?.ahora | fechaHora }}</strong>
          <span class="etiqueta-estado" [class.pendiente]="reloj()?.simulado">{{ reloj()?.simulado ? 'Simulada' : 'Real' }}</span>
        </div>
      </section>

      <section class="tarjeta">
        <h2>Simular una fecha</h2>
        <div class="fila">
          <mat-form-field>
            <mat-label>Fecha y hora simuladas</mat-label>
            <input matInput type="datetime-local" [ngModel]="fecha()" (ngModelChange)="fecha.set($event)" />
            <app-ayuda matSuffix texto="Desde ese momento, ventas, cortes, pagos y mora usan esta fecha. Recuerda volver al reloj real al terminar la demostración." />
          </mat-form-field>
          <button mat-flat-button color="primary" (click)="simular()" [disabled]="!fecha()"><mat-icon>science</mat-icon> Simular</button>
          <button mat-stroked-button (click)="restaurar()" [disabled]="!reloj()?.simulado"><mat-icon>restore</mat-icon> Volver al reloj real</button>
        </div>
        <div class="aviso-info">
          La simulación afecta a todas las tiendas. Los estados de cuenta se generan con el proceso automático (cada hora) o con el
          botón “Generar cortes ahora” del vendedor.
        </div>
      </section>
    </div>
  `,
  styles: `
    .actual {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 16px;
    }
    .actual mat-icon {
      font-size: 40px;
      width: 40px;
      height: 40px;
      color: var(--verde);
    }
    .actual small {
      display: block;
      color: var(--texto-suave);
      font-size: 0.75rem;
      font-weight: 700;
      text-transform: uppercase;
    }
    .actual strong {
      font-size: 1.5rem;
      margin-right: 10px;
    }
  `,
})
export class AdminReloj {
  private readonly api = inject(TiendasApi);
  private readonly avisos = inject(MatSnackBar);

  protected readonly reloj = signal<RelojSistema | null>(null);
  protected readonly fecha = signal('');
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.api.obtenerReloj().subscribe({
      next: (r) => {
        this.reloj.set(r);
        this.fecha.set(r.ahora.substring(0, 16));
      },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected simular(): void {
    const valor = this.fecha().length === 16 ? `${this.fecha()}:00` : this.fecha();
    this.api.simularReloj(valor).subscribe({
      next: (r) => {
        this.reloj.set(r);
        this.avisos.open('Fecha simulada activada', 'Cerrar');
      },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected restaurar(): void {
    this.api.restaurarReloj().subscribe({
      next: (r) => {
        this.reloj.set(r);
        this.fecha.set(r.ahora.substring(0, 16));
        this.avisos.open('Se volvió al reloj real', 'Cerrar');
      },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
