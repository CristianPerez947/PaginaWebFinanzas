import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { forkJoin } from 'rxjs';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { MonedaPipe, TasaPipe, formatearMoneda } from '../../../compartido/infraestructura/formato';
import { confirmar } from '../../../compartido/presentacion/componentes/confirmar-dialogo';
import { Cliente, nombreCompleto } from '../../../clientes/dominio/modelo/cliente.entidad';
import { ClientesApi } from '../../../clientes/infraestructura/clientes-api';
import { EstadoCuentaAcciones } from '../../../estadocuenta/aplicacion/estado-cuenta-acciones';
import { EstadoCuenta } from '../../../estadocuenta/dominio/modelo/estado-cuenta.entidad';
import { EstadoCuentaApi } from '../../../estadocuenta/infraestructura/estado-cuenta-api';
import { ListaEstadosCuenta } from '../../../estadocuenta/presentacion/componentes/lista-estados-cuenta';
import { Cuota, DeudaCliente, Obligacion, ResumenPago, Venta } from '../../dominio/modelo/credito.entidad';
import { CreditoApi } from '../../infraestructura/credito-api';
import { CondonarDialogo } from '../componentes/condonar-dialogo';
import { DetalleObligacion } from '../componentes/detalle-obligacion';
import { ListaCompras } from '../componentes/lista-compras';
import { PagoDialogo } from '../componentes/pago-dialogo';

/**
 * Ficha del cliente para el vendedor: su deuda al día (con mora), plan de pagos, registro de pagos,
 * condonaciones, compras y estados de cuenta.
 */
@Component({
  selector: 'app-ficha-cliente',
  imports: [
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MonedaPipe,
    TasaPipe,
    DetalleObligacion,
    ListaCompras,
    ListaEstadosCuenta,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <a routerLink="/vendedor/clientes" class="volver"><mat-icon inline>arrow_back</mat-icon> Clientes</a>
      @if (error()) { <div class="aviso-error">{{ error() }}</div> }

      @if (cliente(); as c) {
        <div class="pagina-encabezado">
          <div>
            <h1>{{ nombre() }}</h1>
            <p>
              DNI {{ c.dni }} · Tel. {{ c.telefono }} ·
              <span class="etiqueta-estado" [class.baja]="!c.activo">{{ c.activo ? 'Activo' : 'De baja' }}</span>
            </p>
          </div>
          <div class="fila">
            <a mat-flat-button color="primary" [routerLink]="['/vendedor/ventas/nueva']" [queryParams]="{ clienteId: c.id }" [disabled]="!c.activo">
              <mat-icon>point_of_sale</mat-icon> Nueva venta
            </a>
            <button mat-stroked-button (click)="generarEstadoCuenta()"><mat-icon>description</mat-icon> Generar estado de cuenta</button>
            @if (pendientes().length) {
              <button mat-stroked-button color="warn" (click)="condonarTodo()"><mat-icon>volunteer_activism</mat-icon> Condonar toda la deuda</button>
            }
          </div>
        </div>
      }

      @if (deuda(); as d) {
        @if (d.enMora) {
          <div class="aviso-error">
            <mat-icon inline>warning</mat-icon> Cliente con mora: {{ d.totalVencido | moneda: d.moneda }} vencidos a la fecha del sistema.
            No puede recibir crédito nuevo hasta regularizar sus pagos.
          </div>
        }
        <div class="cuadricula kpis">
          <div class="kpi" [class.alerta]="d.capitalPendiente > 0"><span class="etiqueta">Deuda pendiente</span><span class="valor">{{ d.capitalPendiente | moneda: d.moneda }}</span><span class="nota">Capital</span></div>
          <div class="kpi bien"><span class="etiqueta">Disponible</span><span class="valor">{{ d.disponible | moneda: d.moneda }}</span><span class="nota">Límite {{ d.limiteCredito | moneda: d.moneda }}</span></div>
          <div class="kpi" [class.alerta]="d.totalVencido > 0"><span class="etiqueta">Vencido con mora</span><span class="valor">{{ d.totalVencido | moneda: d.moneda }}</span><span class="nota">A {{ d.fechaConsulta }}</span></div>
          @if (cliente(); as c) {
            <div class="kpi"><span class="etiqueta">Tasa pactada</span><span class="valor">{{ c.tasaCompensatoria | tasa }}</span><span class="nota">{{ c.tipoTasa === 'EFECTIVA' ? 'Efectiva anual' : 'Nominal anual' }} · mora {{ c.tasaMora | tasa }}</span></div>
            <div class="kpi"><span class="etiqueta">Ciclo</span><span class="valor">Corte {{ c.diaCorte }} · Pago {{ c.diaPago }}</span><span class="nota">Hasta {{ c.maxMeses }} meses · {{ c.horaCorte }}</span></div>
          }
        </div>
      }

      <mat-tab-group class="pestanas" animationDuration="150ms">
        <mat-tab label="Deuda y pagos">
          <div class="contenido-pestana">
            @for (o of pendientes(); track o.id) {
              <app-detalle-obligacion
                [obligacion]="o"
                [vendedor]="true"
                (pagarCiclo)="pagarCiclo($event)"
                (pagarCuota)="pagarCuota($event.obligacion, $event.cuota)"
                (condonar)="condonar($event)"
              />
            } @empty {
              <div class="vacio tarjeta"><mat-icon>task_alt</mat-icon><p>Este cliente no tiene deudas pendientes.</p></div>
            }
          </div>
        </mat-tab>
        <mat-tab label="Historial de deudas">
          <div class="contenido-pestana">
            @for (o of cerradas(); track o.id) { <app-detalle-obligacion [obligacion]="o" /> }
            @empty { <div class="vacio tarjeta"><mat-icon>history</mat-icon><p>Sin deudas pagadas ni condonadas.</p></div> }
          </div>
        </mat-tab>
        <mat-tab label="Compras">
          <div class="contenido-pestana"><app-lista-compras [ventas]="ventas()" /></div>
        </mat-tab>
        <mat-tab label="Estados de cuenta">
          <div class="contenido-pestana">
            <app-lista-estados-cuenta
              [estados]="estados()"
              [mostrarCliente]="false"
              [puedeRegenerar]="true"
              (ver)="acciones.ver($event, false)"
              (pdf)="acciones.descargar($event, false)"
              (regenerar)="regenerar($event)"
            />
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: `
    .volver {
      color: var(--texto-suave);
      text-decoration: none;
      display: inline-block;
      margin-bottom: 8px;
    }
    .pestanas {
      margin-top: 18px;
    }
    .contenido-pestana {
      padding: 16px 0;
      display: flex;
      flex-direction: column;
      gap: 14px;
    }
  `,
})
export class FichaCliente {
  readonly id = input.required<string>();

  private readonly clientesApi = inject(ClientesApi);
  private readonly creditoApi = inject(CreditoApi);
  private readonly estadosApi = inject(EstadoCuentaApi);
  private readonly dialogo = inject(MatDialog);
  private readonly avisos = inject(MatSnackBar);
  protected readonly acciones = inject(EstadoCuentaAcciones);

  protected readonly cliente = signal<Cliente | null>(null);
  protected readonly deuda = signal<DeudaCliente | null>(null);
  protected readonly ventas = signal<Venta[]>([]);
  protected readonly estados = signal<EstadoCuenta[]>([]);
  protected readonly error = signal<string | null>(null);

  protected readonly nombre = computed(() => (this.cliente() ? nombreCompleto(this.cliente()!) : ''));
  protected readonly pendientes = computed(() => this.deuda()?.obligaciones.filter((o) => o.estado === 'PENDIENTE') ?? []);
  protected readonly cerradas = computed(() =>
    (this.deuda()?.obligaciones.filter((o) => o.estado !== 'PENDIENTE') ?? []).slice().reverse(),
  );

  constructor() {
    effect(() => this.cargar());
  }

  private cargar(): void {
    const id = Number(this.id());
    forkJoin({
      cliente: this.clientesApi.obtener(id),
      deuda: this.creditoApi.deudaDeCliente(id),
      ventas: this.creditoApi.listarVentas(id),
      estados: this.estadosApi.listar(id),
    }).subscribe({
      next: (r) => {
        this.cliente.set(r.cliente);
        this.deuda.set(r.deuda);
        this.ventas.set(r.ventas);
        this.estados.set(r.estados);
        this.error.set(null);
      },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  // ----- Pagos -----

  protected pagarCiclo(obligacion: Obligacion): void {
    const d = this.deuda();
    if (!d) return;
    const fechaCiclo = obligacion.fechaPago;
    // El pago cancela todas las compras a fin de mes que vencen en la misma fecha.
    const mismas = this.pendientes().filter((o) => o.modalidad === 'FIN_DE_MES' && o.fechaPago === fechaCiclo);
    const importe = mismas.reduce((suma, o) => suma + o.importeExigible, 0);
    this.dialogo
      .open(PagoDialogo, {
        width: '480px',
        data: {
          titulo: 'Registrar pago del ciclo',
          descripcion: `Se cancelan ${mismas.length} compra(s) a fin de mes con vencimiento el ${fechaCiclo.split('-').reverse().join('/')}.`,
          importe,
          moneda: d.moneda,
          fechaMaxima: d.fechaConsulta,
        },
      })
      .afterClosed()
      .subscribe((fecha?: string) => {
        if (!fecha) return;
        this.creditoApi.pagarCiclo(d.clienteId, fechaCiclo, fecha).subscribe({
          next: (r) => this.pagoRegistrado(r, d.moneda),
          error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar', { duration: 8000 }),
        });
      });
  }

  protected pagarCuota(obligacion: Obligacion, cuota: Cuota): void {
    const d = this.deuda();
    if (!d || obligacion.id === null) return;
    this.dialogo
      .open(PagoDialogo, {
        width: '480px',
        data: {
          titulo: `Registrar pago de la cuota ${cuota.numero}/${obligacion.numCuotas}`,
          descripcion: `Cuota con vencimiento el ${cuota.fechaVencimiento.split('-').reverse().join('/')}.`,
          importe: cuota.totalAPagar,
          moneda: d.moneda,
          fechaMaxima: d.fechaConsulta,
        },
      })
      .afterClosed()
      .subscribe((fecha?: string) => {
        if (!fecha) return;
        this.creditoApi.pagarCuota(obligacion.id!, cuota.numero, fecha).subscribe({
          next: (r) => this.pagoRegistrado(r, d.moneda),
          error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar', { duration: 8000 }),
        });
      });
  }

  private pagoRegistrado(r: ResumenPago, moneda: string): void {
    this.avisos.open(
      `Pago registrado: ${formatearMoneda(r.total, moneda)} (mora ${formatearMoneda(r.mora, moneda)}, interés ${formatearMoneda(r.interes, moneda)}, capital ${formatearMoneda(r.capital, moneda)}).`,
      'Cerrar',
      { duration: 9000 },
    );
    this.cargar();
  }

  // ----- Condonaciones -----

  protected condonar(obligacion: Obligacion): void {
    if (obligacion.id === null) return;
    this.dialogo
      .open(CondonarDialogo, {
        width: '480px',
        data: { titulo: 'Condonar esta deuda', descripcion: 'Se perdonará lo que falta pagar de esta compra.' },
      })
      .afterClosed()
      .subscribe((motivo?: string) => {
        if (!motivo) return;
        this.creditoApi.condonarObligacion(obligacion.id!, motivo).subscribe({
          next: () => {
            this.avisos.open('Deuda condonada', 'Cerrar');
            this.cargar();
          },
          error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar', { duration: 8000 }),
        });
      });
  }

  protected condonarTodo(): void {
    const c = this.cliente();
    if (!c) return;
    this.dialogo
      .open(CondonarDialogo, {
        width: '480px',
        data: {
          titulo: 'Condonar toda la deuda',
          descripcion: `Se perdonarán todas las deudas pendientes de ${nombreCompleto(c)}.`,
        },
      })
      .afterClosed()
      .subscribe((motivo?: string) => {
        if (!motivo) return;
        this.creditoApi.condonarDeudaCliente(c.id, motivo).subscribe({
          next: () => {
            this.avisos.open('Deuda condonada', 'Cerrar');
            this.cargar();
          },
          error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar', { duration: 8000 }),
        });
      });
  }

  // ----- Estados de cuenta -----

  protected generarEstadoCuenta(): void {
    this.generar(undefined);
  }

  protected regenerar(estado: EstadoCuenta): void {
    this.generar(estado.fechaCorte);
  }

  private generar(fechaCorte?: string): void {
    const c = this.cliente();
    if (!c) return;
    confirmar(this.dialogo, {
      titulo: 'Generar estado de cuenta',
      mensaje: fechaCorte
        ? 'Se recalculará este estado de cuenta con la mora a la fecha del sistema.'
        : 'Se generará el estado de cuenta del último corte vencido de este cliente.',
      textoConfirmar: 'Generar',
    }).subscribe((ok) => {
      if (!ok) return;
      this.estadosApi.generar(c.id, fechaCorte).subscribe({
        next: () => {
          this.avisos.open('Estado de cuenta generado', 'Cerrar');
          this.cargar();
        },
        error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar', { duration: 8000 }),
      });
    });
  }
}
