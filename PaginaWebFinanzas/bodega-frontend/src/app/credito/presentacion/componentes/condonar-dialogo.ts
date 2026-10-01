import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AyudaCampo } from '../../../compartido/presentacion/componentes/ayuda-campo';

export interface DatosCondonar {
  titulo: string;
  descripcion: string;
}

/**
 * Condonación de una deuda: no se borra, queda registrada con su motivo en el historial y la auditoría.
 */
@Component({
  selector: 'app-condonar-dialogo',
  imports: [ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, AyudaCampo],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ datos.titulo }}</h2>
    <mat-dialog-content>
      <p>{{ datos.descripcion }}</p>
      <div class="aviso-info">La deuda no se elimina: queda como “Condonada” en el historial y en la auditoría.</div>
      <form [formGroup]="formulario">
        <mat-form-field>
          <mat-label>Motivo de la condonación</mat-label>
          <textarea matInput rows="3" formControlName="motivo" maxlength="280"></textarea>
          <app-ayuda matSuffix texto="Explica por qué se perdona la deuda (por ejemplo: emergencia familiar, error de registro). Es obligatorio y queda en la auditoría." />
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-flat-button color="warn" [disabled]="formulario.invalid" (click)="confirmar()">Condonar deuda</button>
    </mat-dialog-actions>
  `,
})
export class CondonarDialogo {
  protected readonly datos = inject<DatosCondonar>(MAT_DIALOG_DATA);
  private readonly ref = inject<MatDialogRef<CondonarDialogo, string>>(MatDialogRef);
  private readonly fb = inject(FormBuilder);

  protected readonly formulario = this.fb.nonNullable.group({
    motivo: ['', [Validators.required, Validators.minLength(5)]],
  });

  protected confirmar(): void {
    if (this.formulario.valid) this.ref.close(this.formulario.getRawValue().motivo.trim());
  }
}
