import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { FechaHoraPipe } from '../../../compartido/infraestructura/formato';
import { IdentidadStore } from '../../../identidad/aplicacion/identidad-store';
import { RegistroAuditoria } from '../../dominio/modelo/auditoria.entidad';
import { AuditoriaApi } from '../../infraestructura/auditoria-api';

/**
 * Registro de todas las operaciones del sistema: quién hizo qué y cuándo. El vendedor ve su tienda; el
 * administrador del sistema ve todas.
 */
@Component({
  selector: 'app-auditoria',
  imports: [FormsModule, MatFormFieldModule, MatIconModule, MatInputModule, FechaHoraPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Auditoría</h1>
          <p>Constancia de cada operación: inicios de sesión, altas, bajas, ventas, pagos, condonaciones y cortes.</p>
        </div>
      </div>

      <mat-form-field class="buscador">
        <mat-label>Filtrar (usuario, acción o detalle)</mat-label>
        <mat-icon matPrefix>filter_list</mat-icon>
        <input matInput [ngModel]="busqueda()" (ngModelChange)="busqueda.set($event)" />
      </mat-form-field>

      @if (error()) { <div class="aviso-error">{{ error() }}</div> }

      <div class="tabla-contenedor">
        <table class="tabla">
          <thead>
            <tr><th>Fecha y hora</th><th>Usuario</th><th>Rol</th><th>Acción</th>@if (esAdmin()) { <th>Tienda</th> }<th>Detalle</th></tr>
          </thead>
          <tbody>
            @for (r of filtrados(); track r.id) {
              <tr>
                <td class="nowrap">{{ r.fechaHora | fechaHora }}</td>
                <td>{{ r.usuario }}</td>
                <td><span class="etiqueta-estado neutra">{{ r.rol }}</span></td>
                <td><strong>{{ r.accion }}</strong></td>
                @if (esAdmin()) { <td>{{ r.tiendaId ?? '—' }}</td> }
                <td>{{ r.detalle }}</td>
              </tr>
            } @empty {
              <tr><td [attr.colspan]="esAdmin() ? 6 : 5" class="vacio"><mat-icon>fact_check</mat-icon><p>Sin registros.</p></td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: `
    .buscador {
      width: 380px;
      max-width: 100%;
      margin-bottom: 12px;
    }
    .nowrap {
      white-space: nowrap;
    }
  `,
})
export class Auditoria {
  private readonly api = inject(AuditoriaApi);
  private readonly identidad = inject(IdentidadStore);

  protected readonly registros = signal<RegistroAuditoria[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly busqueda = signal('');
  protected readonly esAdmin = computed(() => this.identidad.rol() === 'ADMIN_SISTEMA');

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.registros().filter((r) => !texto || `${r.usuario} ${r.accion} ${r.detalle ?? ''} ${r.rol}`.toLowerCase().includes(texto));
  });

  constructor() {
    this.api.listar(400).subscribe({
      next: (r) => this.registros.set(r),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
