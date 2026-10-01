import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { MonedaPipe } from '../../../compartido/infraestructura/formato';
import { confirmar } from '../../../compartido/presentacion/componentes/confirmar-dialogo';
import { EstadoCuentaAcciones } from '../../aplicacion/estado-cuenta-acciones';
import { EstadoCuenta } from '../../dominio/modelo/estado-cuenta.entidad';
import { EstadoCuentaApi } from '../../infraestructura/estado-cuenta-api';
import { ListaEstadosCuenta } from '../componentes/lista-estados-cuenta';

/**
 * Cobranzas: estados de cuenta que genera el sistema en cada fecha de corte. Desde aquí el vendedor los revisa,
 * los descarga en PDF y entra a la ficha del cliente para registrar el pago.
 */
@Component({
  selector: 'app-cobranzas',
  imports: [FormsModule, MatButtonModule, MatButtonToggleModule, MatFormFieldModule, MatIconModule, MatInputModule, MonedaPipe, ListaEstadosCuenta],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Cobranzas</h1>
          <p>El sistema genera el estado de cuenta de cada cliente en su fecha de corte y lo mantiene al día con la mora.</p>
        </div>
        <button mat-flat-button color="primary" (click)="generarCortes()" [disabled]="generando()">
          <mat-icon>bolt</mat-icon> {{ generando() ? 'Generando…' : 'Generar cortes ahora' }}
        </button>
      </div>

      <div class="cuadricula kpis">
        <div class="kpi"><span class="etiqueta">Por cobrar</span><span class="valor">{{ totalPendiente() | moneda }}</span><span class="nota">{{ pendientes() }} estado(s) pendiente(s)</span></div>
        <div class="kpi" [class.alerta]="conMora() > 0"><span class="etiqueta">Con mora</span><span class="valor">{{ conMora() }}</span><span class="nota">Estados con intereses por mora</span></div>
        <div class="kpi bien"><span class="etiqueta">Pagados</span><span class="valor">{{ pagados() }}</span><span class="nota">Estados ya cancelados</span></div>
      </div>

      <div class="filtros fila">
        <mat-button-toggle-group [value]="filtro()" (change)="filtro.set($event.value)" aria-label="Filtrar por estado">
          <mat-button-toggle value="TODOS">Todos</mat-button-toggle>
          <mat-button-toggle value="PENDIENTE">Pendientes</mat-button-toggle>
          <mat-button-toggle value="PAGADO">Pagados</mat-button-toggle>
        </mat-button-toggle-group>
        <mat-form-field class="buscador">
          <mat-label>Buscar cliente</mat-label>
          <mat-icon matPrefix>search</mat-icon>
          <input matInput [ngModel]="busqueda()" (ngModelChange)="busqueda.set($event)" />
        </mat-form-field>
      </div>

      @if (error()) { <div class="aviso-error">{{ error() }}</div> }

      <app-lista-estados-cuenta
        [estados]="filtrados()"
        [puedeRegenerar]="true"
        (ver)="acciones.ver($event, false)"
        (pdf)="acciones.descargar($event, false)"
        (regenerar)="regenerar($event)"
      />
    </div>
  `,
  styles: `
    .filtros {
      margin: 18px 0 12px;
    }
    .buscador {
      width: 300px;
      max-width: 100%;
    }
  `,
})
export class Cobranzas {
  private readonly api = inject(EstadoCuentaApi);
  private readonly dialogo = inject(MatDialog);
  private readonly avisos = inject(MatSnackBar);
  protected readonly acciones = inject(EstadoCuentaAcciones);

  protected readonly estados = signal<EstadoCuenta[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly generando = signal(false);
  protected readonly filtro = signal<'TODOS' | 'PENDIENTE' | 'PAGADO'>('PENDIENTE');
  protected readonly busqueda = signal('');

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.estados().filter(
      (e) =>
        (this.filtro() === 'TODOS' || e.estado === this.filtro()) &&
        (!texto || `${e.nombreCliente} ${e.dniCliente}`.toLowerCase().includes(texto)),
    );
  });

  protected readonly pendientes = computed(() => this.estados().filter((e) => e.estado === 'PENDIENTE').length);
  protected readonly pagados = computed(() => this.estados().filter((e) => e.estado === 'PAGADO').length);
  protected readonly conMora = computed(() => this.estados().filter((e) => e.estado === 'PENDIENTE' && e.totalMora > 0).length);
  protected readonly totalPendiente = computed(() =>
    this.estados().filter((e) => e.estado === 'PENDIENTE' && e.moneda === 'PEN').reduce((s, e) => s + e.totalAPagar, 0),
  );

  constructor() {
    this.cargar();
  }

  private cargar(): void {
    this.api.listar().subscribe({
      next: (e) => this.estados.set(e),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected generarCortes(): void {
    this.generando.set(true);
    this.api.generarCortes().subscribe({
      next: (r) => {
        this.avisos.open(r.generados ? `Se generaron o actualizaron ${r.generados} estado(s) de cuenta` : 'No había cortes pendientes por generar', 'Cerrar');
        this.generando.set(false);
        this.cargar();
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.generando.set(false);
      },
    });
  }

  protected regenerar(estado: EstadoCuenta): void {
    confirmar(this.dialogo, {
      titulo: 'Regenerar estado de cuenta',
      mensaje: `Se recalculará el estado de cuenta de ${estado.nombreCliente} con la mora a la fecha del sistema.`,
      textoConfirmar: 'Regenerar',
    }).subscribe((ok) => {
      if (!ok) return;
      this.api.generar(estado.clienteId, estado.fechaCorte).subscribe({
        next: () => {
          this.avisos.open('Estado de cuenta actualizado', 'Cerrar');
          this.cargar();
        },
        error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar', { duration: 8000 }),
      });
    });
  }
}
