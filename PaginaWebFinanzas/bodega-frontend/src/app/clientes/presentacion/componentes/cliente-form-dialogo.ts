import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { AyudaCampo } from '../../../compartido/presentacion/componentes/ayuda-campo';
import { Cliente, ClienteConClave, GuardarCliente } from '../../dominio/modelo/cliente.entidad';
import { ClientesApi } from '../../infraestructura/clientes-api';

export interface ResultadoClienteForm {
  cliente: Cliente;
  /** Presente solo al crear: la clave inicial, visible una sola vez. */
  clave?: string;
}

/**
 * Alta y edición de un cliente con sus condiciones de crédito (tasa nominal o efectiva, moneda, límite,
 * plazo máximo, fecha de corte y fecha de pago). Cada campo tiene su ayuda.
 */
@Component({
  selector: 'app-cliente-form-dialogo',
  imports: [ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, AyudaCampo],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ cliente ? 'Editar cliente' : 'Nuevo cliente' }}</h2>
    <mat-dialog-content>
      <form class="formulario-rejilla" [formGroup]="formulario" (ngSubmit)="guardar()" id="form-cliente">
        <div class="formulario-seccion">Datos del cliente</div>

        <mat-form-field>
          <mat-label>DNI</mat-label>
          <input matInput formControlName="dni" inputmode="numeric" maxlength="8" />
          <app-ayuda matSuffix texto="DNI de 8 dígitos. Es el usuario con el que el cliente consulta su cuenta y no se puede cambiar después." />
          @if (formulario.controls.dni.invalid && formulario.controls.dni.touched) { <mat-error>Debe tener 8 dígitos</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Teléfono móvil</mat-label>
          <input matInput formControlName="telefono" inputmode="numeric" maxlength="9" />
          <app-ayuda matSuffix texto="Celular de 9 dígitos (por ejemplo 987654321). Se usa para enviarle la clave y los avisos por WhatsApp." />
          @if (formulario.controls.telefono.invalid && formulario.controls.telefono.touched) { <mat-error>Debe tener 9 dígitos</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Nombres</mat-label>
          <input matInput formControlName="nombres" maxlength="100" />
          <app-ayuda matSuffix texto="Nombres del cliente, como figuran en su DNI." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Apellidos</mat-label>
          <input matInput formControlName="apellidos" maxlength="100" />
          <app-ayuda matSuffix texto="Apellido paterno y materno." />
        </mat-form-field>
        <mat-form-field class="completo">
          <mat-label>Dirección (opcional)</mat-label>
          <input matInput formControlName="direccion" maxlength="200" />
          <app-ayuda matSuffix texto="Dónde vive el cliente. Es opcional." />
        </mat-form-field>

        <div class="formulario-seccion">Condiciones del crédito</div>
        <mat-form-field>
          <mat-label>Moneda del crédito</mat-label>
          <mat-select formControlName="moneda">
            <mat-option value="PEN">Soles (S/)</mat-option>
            <mat-option value="USD">Dólares (US$)</mat-option>
          </mat-select>
          <app-ayuda matSuffix texto="Moneda en la que se le otorga el crédito. Los precios de la tienda están en soles; si eliges dólares se convierten con el tipo de cambio de la tienda." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Límite de crédito</mat-label>
          <input matInput type="number" step="0.01" min="1" formControlName="limiteCredito" />
          <app-ayuda matSuffix texto="Máximo de deuda pendiente que puede tener a la vez. Una compra que lo supere no se permite." />
          @if (formulario.controls.limiteCredito.invalid) { <mat-error>Ingresa un monto mayor que cero</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Máximo de meses (cuotas)</mat-label>
          <input matInput type="number" min="1" max="24" formControlName="maxMeses" />
          <app-ayuda matSuffix texto="Número máximo de cuotas mensuales que se le pueden dar en una compra (entre 1 y 24)." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Hora límite del corte</mat-label>
          <input matInput type="time" formControlName="horaCorte" />
          <app-ayuda matSuffix texto="Hora del día de corte hasta la cual una compra entra al ciclo actual. Después de esa hora pasa al ciclo siguiente." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Día de corte</mat-label>
          <input matInput type="number" min="1" max="28" formControlName="diaCorte" />
          <app-ayuda matSuffix texto="Día del mes (1 a 28) en que se cierra el ciclo y se prepara el estado de cuenta." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Día de pago</mat-label>
          <input matInput type="number" min="1" max="28" formControlName="diaPago" />
          <app-ayuda matSuffix texto="Día del mes (1 a 28) en que el cliente se compromete a pagar todos los meses." />
        </mat-form-field>

        <div class="formulario-seccion">Interés compensatorio (por el crédito)</div>
        <mat-form-field>
          <mat-label>Tipo de tasa</mat-label>
          <mat-select formControlName="tipoTasa">
            <mat-option value="EFECTIVA">Efectiva anual (TEA)</mat-option>
            <mat-option value="NOMINAL">Nominal anual (TNA)</mat-option>
          </mat-select>
          <app-ayuda matSuffix texto="Efectiva: el porcentaje ya incluye la capitalización. Nominal: se convierte a efectiva según su capitalización." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Tasa anual (%)</mat-label>
          <input matInput type="number" step="0.0000001" min="0.0000001" formControlName="tasaCompensatoria" />
          <app-ayuda matSuffix texto="Porcentaje anual pactado, por ejemplo 24 para 24 %. Se admiten hasta 7 decimales." />
        </mat-form-field>
        @if (esNominal()) {
          <mat-form-field class="completo">
            <mat-label>Capitalización de la tasa nominal</mat-label>
            <mat-select formControlName="capitalizacion">
              <mat-option value="DIARIA">Diaria</mat-option>
              <mat-option value="MENSUAL">Mensual</mat-option>
            </mat-select>
            <app-ayuda matSuffix texto="Cada cuánto se capitalizan los intereses de la tasa nominal (base de 360 días)." />
          </mat-form-field>
        }

        <div class="formulario-seccion">Interés moratorio (por pagar tarde)</div>
        <mat-form-field>
          <mat-label>Tipo de tasa moratoria</mat-label>
          <mat-select formControlName="tipoTasaMora">
            <mat-option value="EFECTIVA">Efectiva anual (TEA)</mat-option>
            <mat-option value="NOMINAL">Nominal anual (TNA)</mat-option>
          </mat-select>
          <app-ayuda matSuffix texto="Tasa que se cobra por los días de atraso después de la fecha de pago." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Tasa moratoria anual (%)</mat-label>
          <input matInput type="number" step="0.0000001" min="0.0000001" formControlName="tasaMora" />
          <app-ayuda matSuffix texto="Porcentaje anual de mora, por ejemplo 36 para 36 %." />
        </mat-form-field>
        @if (esNominalMora()) {
          <mat-form-field class="completo">
            <mat-label>Capitalización de la mora nominal</mat-label>
            <mat-select formControlName="capitalizacionMora">
              <mat-option value="DIARIA">Diaria</mat-option>
              <mat-option value="MENSUAL">Mensual</mat-option>
            </mat-select>
            <app-ayuda matSuffix texto="Capitalización de la tasa moratoria nominal." />
          </mat-form-field>
        }
      </form>
      @if (error()) {
        <div class="aviso-error">{{ error() }}</div>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-flat-button color="primary" type="submit" form="form-cliente" [disabled]="formulario.invalid || guardando()">
        {{ guardando() ? 'Guardando…' : cliente ? 'Guardar cambios' : 'Crear cliente' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ClienteFormDialogo {
  protected readonly cliente = inject<Cliente | null>(MAT_DIALOG_DATA, { optional: true });
  private readonly ref = inject<MatDialogRef<ClienteFormDialogo, ResultadoClienteForm>>(MatDialogRef);
  private readonly api = inject(ClientesApi);
  private readonly fb = inject(FormBuilder);

  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = this.fb.nonNullable.group({
    dni: [{ value: this.cliente?.dni ?? '', disabled: !!this.cliente }, [Validators.required, Validators.pattern(/^\d{8}$/)]],
    nombres: [this.cliente?.nombres ?? '', Validators.required],
    apellidos: [this.cliente?.apellidos ?? '', Validators.required],
    telefono: [this.cliente?.telefono ?? '', [Validators.required, Validators.pattern(/^\d{9}$/)]],
    direccion: [this.cliente?.direccion ?? ''],
    moneda: [this.cliente?.moneda ?? 'PEN'],
    limiteCredito: [this.cliente?.limiteCredito ?? 500, [Validators.required, Validators.min(1)]],
    maxMeses: [this.cliente?.maxMeses ?? 3, [Validators.required, Validators.min(1), Validators.max(24)]],
    diaCorte: [this.cliente?.diaCorte ?? 20, [Validators.required, Validators.min(1), Validators.max(28)]],
    horaCorte: [this.cliente?.horaCorte?.substring(0, 5) ?? '23:59', Validators.required],
    diaPago: [this.cliente?.diaPago ?? 26, [Validators.required, Validators.min(1), Validators.max(28)]],
    tipoTasa: [this.cliente?.tipoTasa ?? 'EFECTIVA'],
    tasaCompensatoria: [this.cliente?.tasaCompensatoria ?? 24, [Validators.required, Validators.min(0.0000001)]],
    capitalizacion: [this.cliente?.capitalizacion ?? 'MENSUAL'],
    tipoTasaMora: [this.cliente?.tipoTasaMora ?? 'EFECTIVA'],
    tasaMora: [this.cliente?.tasaMora ?? 36, [Validators.required, Validators.min(0.0000001)]],
    capitalizacionMora: [this.cliente?.capitalizacionMora ?? 'MENSUAL'],
  });

  private readonly tipoTasa = toSignal(this.formulario.controls.tipoTasa.valueChanges, { initialValue: this.formulario.controls.tipoTasa.value });
  private readonly tipoTasaMora = toSignal(this.formulario.controls.tipoTasaMora.valueChanges, { initialValue: this.formulario.controls.tipoTasaMora.value });
  protected readonly esNominal = computed(() => this.tipoTasa() === 'NOMINAL');
  protected readonly esNominalMora = computed(() => this.tipoTasaMora() === 'NOMINAL');

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const v = this.formulario.getRawValue();
    const datos: GuardarCliente & { dni: string } = {
      dni: v.dni,
      nombres: v.nombres.trim(),
      apellidos: v.apellidos.trim(),
      telefono: v.telefono,
      direccion: v.direccion.trim() || null,
      moneda: v.moneda,
      limiteCredito: Number(v.limiteCredito),
      maxMeses: Number(v.maxMeses),
      diaCorte: Number(v.diaCorte),
      horaCorte: v.horaCorte,
      diaPago: Number(v.diaPago),
      tipoTasa: v.tipoTasa,
      tasaCompensatoria: Number(v.tasaCompensatoria),
      capitalizacion: v.tipoTasa === 'NOMINAL' ? v.capitalizacion : null,
      tipoTasaMora: v.tipoTasaMora,
      tasaMora: Number(v.tasaMora),
      capitalizacionMora: v.tipoTasaMora === 'NOMINAL' ? v.capitalizacionMora : null,
    };
    this.guardando.set(true);
    this.error.set(null);
    if (this.cliente) {
      this.api.actualizar(this.cliente.id, datos).subscribe({
        next: (c) => this.ref.close({ cliente: c }),
        error: (e) => this.fallo(e),
      });
    } else {
      this.api.crear(datos).subscribe({
        next: (r: ClienteConClave) => this.ref.close({ cliente: r.cliente, clave: r.clave }),
        error: (e) => this.fallo(e),
      });
    }
  }

  private fallo(e: unknown): void {
    this.error.set(mensajeDeError(e));
    this.guardando.set(false);
  }
}
