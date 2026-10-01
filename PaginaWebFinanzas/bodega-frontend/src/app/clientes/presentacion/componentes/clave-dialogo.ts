import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { Cliente, nombreCompleto } from '../../dominio/modelo/cliente.entidad';

export interface DatosClave {
  cliente: Cliente;
  clave: string;
  nombreTienda: string;
  urlTienda: string;
}

/**
 * Muestra la clave del cliente una sola vez (se guarda cifrada) para que el vendedor se la comunique por
 * teléfono. Incluye un mensaje listo para copiar y enviar por WhatsApp.
 */
@Component({
  selector: 'app-clave-dialogo',
  imports: [MatButtonModule, MatDialogModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>Clave de acceso del cliente</h2>
    <mat-dialog-content>
      <p>
        Entrega esta clave a <strong>{{ nombre }}</strong> por teléfono. Por seguridad <strong>no se vuelve a mostrar</strong>:
        si la olvida, genera una nueva desde la lista de clientes.
      </p>
      <div class="clave">{{ datos.clave }}</div>
      <p class="texto-suave">El cliente ingresa con su DNI <strong>{{ datos.cliente.dni }}</strong> y esta clave.</p>
      <div class="mensaje">{{ mensaje }}</div>
      @if (copiado()) { <div class="aviso-ok">Mensaje copiado. Pégalo en WhatsApp.</div> }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="copiar()"><mat-icon>content_copy</mat-icon> Copiar mensaje</button>
      <button mat-flat-button color="primary" mat-dialog-close>Listo</button>
    </mat-dialog-actions>
  `,
  styles: `
    .clave {
      font-size: 2.2rem;
      font-weight: 800;
      letter-spacing: 0.3em;
      text-align: center;
      background: var(--verde-suave);
      color: var(--verde);
      border-radius: 14px;
      padding: 14px;
      margin: 12px 0;
    }
    .mensaje {
      background: #f1f0e8;
      border-radius: 10px;
      padding: 12px;
      font-size: 0.9rem;
      white-space: pre-line;
    }
  `,
})
export class ClaveDialogo {
  protected readonly datos = inject<DatosClave>(MAT_DIALOG_DATA);
  protected readonly copiado = signal(false);
  protected readonly nombre = nombreCompleto(this.datos.cliente);

  protected readonly mensaje =
    `Hola ${this.datos.cliente.nombres}, ya puedes consultar tu cuenta de ${this.datos.nombreTienda}.\n` +
    `Ingresa a ${this.datos.urlTienda}\n` +
    `Tu DNI: ${this.datos.cliente.dni}\nTu clave: ${this.datos.clave}`;

  protected copiar(): void {
    navigator.clipboard?.writeText(this.mensaje).then(() => this.copiado.set(true), () => this.copiado.set(false));
  }
}
