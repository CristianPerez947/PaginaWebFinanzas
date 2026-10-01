import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';

export interface DatosConfirmar {
  titulo: string;
  mensaje: string;
  textoConfirmar?: string;
  peligro?: boolean;
}

@Component({
  selector: 'app-confirmar-dialogo',
  imports: [MatDialogModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ datos.titulo }}</h2>
    <mat-dialog-content>
      <p style="white-space: pre-line; margin: 0">{{ datos.mensaje }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">Cancelar</button>
      <button mat-flat-button [mat-dialog-close]="true" [color]="datos.peligro ? 'warn' : 'primary'">
        {{ datos.textoConfirmar ?? 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ConfirmarDialogo {
  protected readonly datos = inject<DatosConfirmar>(MAT_DIALOG_DATA);
}

/** Abre un diálogo de confirmación y emite verdadero si el usuario acepta. */
export function confirmar(dialogo: MatDialog, datos: DatosConfirmar): Observable<boolean> {
  return dialogo
    .open(ConfirmarDialogo, { data: datos, width: '440px', maxWidth: '94vw' })
    .afterClosed()
    .pipe(map((resultado) => resultado === true));
}
