import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MonedaPipe } from '../../../compartido/infraestructura/formato';
import { AyudaCampo } from '../../../compartido/presentacion/componentes/ayuda-campo';

export interface DatosPago {
  titulo: string;
  descripcion: string;
  importe: number;
  moneda: string;
  /** Fecha actual del sistema (yyyy-MM-dd): el pago no puede ser posterior. */
  fechaMaxima: string;
}

/**
 * Registro de un pago: el cliente paga completo lo que corresponde; el sistema calcula la mora según la
 * fecha real en que pagó y desglosa mora, interés compensatorio y capital.
 */
@Component({
  selector: 'app-pago-dialogo',
  imports: [ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MonedaPipe, AyudaCampo],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ datos.titulo }}</h2>
    <mat-dialog-content>
      <p>{{ datos.descripcion }}</p>
      <div class="aviso-info">
        Importe exigible a la fecha del sistema: <strong>{{ datos.importe | moneda: datos.moneda }}</strong>.
        No se aceptan pagos parciales.
      </div>
      <form [formGroup]="formulario" (ngSubmit)="confirmar()">
        <mat-form-field>
          <mat-label>Fecha real del pago</mat-label>
          <input matInput type="date" formControlName="fecha" [max]="datos.fechaMaxima" />
          <app-ayuda matSuffix texto="Día en que el cliente pagó. Si pagó después de la fecha de vencimiento, el sistema suma los intereses por mora de esos días. No puede ser posterior a hoy." />
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-flat-button color="primary" [disabled]="formulario.invalid" (click)="confirmar()">Registrar pago</button>
    </mat-dialog-actions>
  `,
})
export class PagoDialogo {
  protected readonly datos = inject<DatosPago>(MAT_DIALOG_DATA);
  private readonly ref = inject<MatDialogRef<PagoDialogo, string>>(MatDialogRef);
  private readonly fb = inject(FormBuilder);

  protected readonly formulario = this.fb.nonNullable.group({
    fecha: [this.datos.fechaMaxima, Validators.required],
  });

  protected confirmar(): void {
    if (this.formulario.valid) this.ref.close(this.formulario.getRawValue().fecha);
  }
}
