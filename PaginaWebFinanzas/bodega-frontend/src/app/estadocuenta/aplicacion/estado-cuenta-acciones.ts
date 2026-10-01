import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { descargarBlob } from '../../compartido/infraestructura/formato';
import { mensajeDeError } from '../../compartido/infraestructura/base-api';
import { EstadoCuenta } from '../dominio/modelo/estado-cuenta.entidad';
import { EstadoCuentaApi } from '../infraestructura/estado-cuenta-api';
import { EstadoCuentaDialogo } from '../presentacion/componentes/estado-cuenta-dialogo';

/**
 * Acciones compartidas sobre un estado de cuenta: ver su detalle y descargar el PDF, tanto para el vendedor
 * como para el cliente (cada uno usa su propio endpoint).
 */
@Injectable({ providedIn: 'root' })
export class EstadoCuentaAcciones {
  private readonly dialogo = inject(MatDialog);
  private readonly api = inject(EstadoCuentaApi);
  private readonly avisos = inject(MatSnackBar);

  descargar(estado: EstadoCuenta, comoCliente: boolean): void {
    const peticion = comoCliente ? this.api.miPdf(estado.id) : this.api.pdf(estado.id);
    peticion.subscribe({
      next: (blob) => descargarBlob(blob, `estado-de-cuenta-${estado.fechaCorte}.pdf`),
      error: (e) => this.avisos.open(mensajeDeError(e), 'Cerrar'),
    });
  }

  ver(estado: EstadoCuenta, comoCliente: boolean): void {
    this.dialogo.open(EstadoCuentaDialogo, {
      data: { estado, descargar: () => this.descargar(estado, comoCliente) },
      width: '780px',
      maxWidth: '96vw',
    });
  }
}
