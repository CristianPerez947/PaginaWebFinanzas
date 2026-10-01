import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { forkJoin } from 'rxjs';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { FechaPipe, MonedaPipe } from '../../../compartido/infraestructura/formato';
import { Cliente, nombreCompleto } from '../../../clientes/dominio/modelo/cliente.entidad';
import { ClientesApi } from '../../../clientes/infraestructura/clientes-api';
import { EstadoCuentaAcciones } from '../../../estadocuenta/aplicacion/estado-cuenta-acciones';
import { EstadoCuenta } from '../../../estadocuenta/dominio/modelo/estado-cuenta.entidad';
import { EstadoCuentaApi } from '../../../estadocuenta/infraestructura/estado-cuenta-api';
import { ListaEstadosCuenta } from '../../../estadocuenta/presentacion/componentes/lista-estados-cuenta';
import { DeudaCliente, Venta } from '../../dominio/modelo/credito.entidad';
import { CreditoApi } from '../../infraestructura/credito-api';
import { DetalleObligacion } from '../componentes/detalle-obligacion';
import { ListaCompras } from '../componentes/lista-compras';

interface ProximoPago {
  fecha: string;
  monto: number;
  concepto: string;
}

/**
 * Cuenta del cliente: su deuda, sus cuotas, su historial de compras y sus estados de cuenta (solo lectura).
 */
@Component({
  selector: 'app-mi-cuenta',
  imports: [RouterLink, MatIconModule, MatTabsModule, MonedaPipe, FechaPipe, DetalleObligacion, ListaCompras, ListaEstadosCuenta],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      @if (error()) {
        <div class="aviso-error">{{ error() }}</div>
      }
      @if (perfil(); as p) {
        <div class="pagina-encabezado">
          <div>
            <h1>Hola, {{ nombre(p) }}</h1>
            <p>DNI {{ p.dni }} · Tu crédito es en {{ p.moneda === 'USD' ? 'dólares' : 'soles' }}. Corte el día {{ p.diaCorte }}, pago el día {{ p.diaPago }}.</p>
          </div>
        </div>
      }

      @if (deuda(); as d) {
        @if (d.enMora) {
          <div class="aviso-error">
            <mat-icon inline>warning</mat-icon>
            Tienes pagos vencidos por {{ d.totalVencido | moneda: d.moneda }} (incluye intereses por mora). Acércate a la
            bodega para regularizarlos; mientras tanto no podrás recibir nuevo crédito.
          </div>
        }
        <div class="cuadricula kpis">
          <div class="kpi" [class.alerta]="d.capitalPendiente > 0">
            <span class="etiqueta">Deuda pendiente</span>
            <span class="valor">{{ d.capitalPendiente | moneda: d.moneda }}</span>
            <span class="nota">Capital que aún debes</span>
          </div>
          <div class="kpi bien">
            <span class="etiqueta">Crédito disponible</span>
            <span class="valor">{{ d.disponible | moneda: d.moneda }}</span>
            <span class="nota">De un límite de {{ d.limiteCredito | moneda: d.moneda }}</span>
          </div>
          <div class="kpi" [class.alerta]="d.totalVencido > 0">
            <span class="etiqueta">Vencido</span>
            <span class="valor">{{ d.totalVencido | moneda: d.moneda }}</span>
            <span class="nota">Con mora incluida</span>
          </div>
          <div class="kpi">
            <span class="etiqueta">Próximo pago</span>
            @if (proximoPago(); as pp) {
              <span class="valor">{{ pp.monto | moneda: d.moneda }}</span>
              <span class="nota">{{ pp.concepto }} · {{ pp.fecha | fecha }}</span>
            } @else {
              <span class="valor">—</span>
              <span class="nota">No tienes pagos pendientes</span>
            }
          </div>
        </div>
      }

      <mat-tab-group class="pestanas" animationDuration="150ms">
        <mat-tab label="Mi deuda">
          <div class="contenido-pestana">
            @for (o of pendientes(); track o.id) {
              <app-detalle-obligacion [obligacion]="o" />
            } @empty {
              <div class="vacio tarjeta"><mat-icon>celebration</mat-icon><p>No tienes deudas pendientes. ¡Gracias por tu puntualidad!</p></div>
            }
          </div>
        </mat-tab>
        <mat-tab label="Mis compras">
          <div class="contenido-pestana"><app-lista-compras [ventas]="compras()" /></div>
        </mat-tab>
        <mat-tab label="Deudas pagadas">
          <div class="contenido-pestana">
            @for (o of cerradas(); track o.id) {
              <app-detalle-obligacion [obligacion]="o" />
            } @empty {
              <div class="vacio tarjeta"><mat-icon>history</mat-icon><p>Todavía no hay deudas pagadas o condonadas.</p></div>
            }
          </div>
        </mat-tab>
        <mat-tab label="Estados de cuenta">
          <div class="contenido-pestana">
            <app-lista-estados-cuenta
              [estados]="estados()"
              [mostrarCliente]="false"
              (ver)="acciones.ver($event, true)"
              (pdf)="acciones.descargar($event, true)"
            />
          </div>
        </mat-tab>
      </mat-tab-group>
      <p class="texto-suave pie">
        Solo puedes consultar tu información. Los pagos se realizan en la bodega y el vendedor los registra.
        <a [routerLink]="['/t', slug()]">Ver productos</a>
      </p>
    </div>
  `,
  styles: `
    .pestanas {
      margin-top: 18px;
    }
    .contenido-pestana {
      padding: 16px 0;
      display: flex;
      flex-direction: column;
      gap: 14px;
    }
    .pie {
      margin-top: 24px;
      text-align: center;
    }
  `,
})
export class MiCuenta {
  readonly slug = input.required<string>();

  private readonly credito = inject(CreditoApi);
  private readonly clientes = inject(ClientesApi);
  private readonly estadosApi = inject(EstadoCuentaApi);
  protected readonly acciones = inject(EstadoCuentaAcciones);

  protected readonly perfil = signal<Cliente | null>(null);
  protected readonly deuda = signal<DeudaCliente | null>(null);
  protected readonly compras = signal<Venta[]>([]);
  protected readonly estados = signal<EstadoCuenta[]>([]);
  protected readonly error = signal<string | null>(null);

  protected readonly pendientes = computed(() => this.deuda()?.obligaciones.filter((o) => o.estado === 'PENDIENTE') ?? []);
  protected readonly cerradas = computed(() =>
    (this.deuda()?.obligaciones.filter((o) => o.estado !== 'PENDIENTE') ?? []).slice().reverse(),
  );

  protected readonly proximoPago = computed<ProximoPago | null>(() => {
    const candidatos: ProximoPago[] = [];
    for (const o of this.pendientes()) {
      if (o.modalidad === 'FIN_DE_MES') {
        candidatos.push({ fecha: o.fechaPago, monto: o.importeExigible, concepto: 'Compra a fin de mes' });
      } else {
        const cuota = o.cuotas.find((c) => c.estado === 'PENDIENTE');
        if (cuota) candidatos.push({ fecha: cuota.fechaVencimiento, monto: cuota.totalAPagar, concepto: `Cuota ${cuota.numero}/${o.numCuotas}` });
      }
    }
    return candidatos.sort((a, b) => a.fecha.localeCompare(b.fecha))[0] ?? null;
  });

  protected nombre(p: Cliente): string {
    return nombreCompleto(p);
  }

  constructor() {
    forkJoin({
      perfil: this.clientes.miPerfil(),
      deuda: this.credito.miDeuda(),
      compras: this.credito.misCompras(),
      estados: this.estadosApi.misEstadosCuenta(),
    }).subscribe({
      next: (r) => {
        this.perfil.set(r.perfil);
        this.deuda.set(r.deuda);
        this.compras.set(r.compras);
        this.estados.set(r.estados);
      },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
