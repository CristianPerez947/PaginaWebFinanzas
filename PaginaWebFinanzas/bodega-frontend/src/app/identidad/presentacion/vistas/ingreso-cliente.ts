import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { AyudaCampo } from '../../../compartido/presentacion/componentes/ayuda-campo';
import { TiendaActualStore } from '../../../tiendas/aplicacion/tienda-actual-store';
import { IdentidadStore } from '../../aplicacion/identidad-store';

/**
 * Ingreso del cliente a la cuenta de una bodega con su DNI y la clave que le entregó el vendedor.
 */
@Component({
  selector: 'app-ingreso-cliente',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, AyudaCampo],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="centro">
      <form class="tarjeta" [formGroup]="formulario" (ngSubmit)="ingresar()">
        <h2><mat-icon>lock</mat-icon> Consultar mi cuenta</h2>
        <p class="texto-suave">
          Ingresa con tu DNI y la clave que te dio {{ tienda()?.nombre ?? 'la bodega' }}. Si la olvidaste, pídele al
          vendedor que te dé una nueva.
        </p>

        <mat-form-field>
          <mat-label>DNI</mat-label>
          <input matInput formControlName="dni" inputmode="numeric" maxlength="8" autocomplete="username" />
          <app-ayuda matSuffix texto="Tu DNI de 8 dígitos, sin espacios ni guiones. Es el mismo que registraste en la bodega." />
          @if (formulario.controls.dni.touched && formulario.controls.dni.invalid) {
            <mat-error>Ingresa los 8 dígitos de tu DNI</mat-error>
          }
        </mat-form-field>

        <mat-form-field>
          <mat-label>Clave</mat-label>
          <input matInput formControlName="clave" [type]="verClave() ? 'text' : 'password'" autocomplete="current-password" />
          <button mat-icon-button matSuffix type="button" (click)="verClave.set(!verClave())" [attr.aria-label]="'Mostrar u ocultar la clave'">
            <mat-icon>{{ verClave() ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          <mat-hint>La clave de 6 dígitos que te envió el vendedor</mat-hint>
        </mat-form-field>

        @if (error()) {
          <div class="aviso-error">{{ error() }}</div>
        }

        <button mat-flat-button color="primary" type="submit" [disabled]="formulario.invalid || enviando()">
          {{ enviando() ? 'Ingresando…' : 'Ingresar' }}
        </button>
      </form>
    </div>
  `,
  styles: `
    .centro {
      display: grid;
      place-items: center;
      padding: 32px 16px;
    }
    form {
      width: 100%;
      max-width: 420px;
      display: flex;
      flex-direction: column;
      gap: 14px;
    }
    h2 {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    p {
      margin: 0;
    }
  `,
})
export class IngresoCliente {
  readonly slug = input.required<string>();

  private readonly fb = inject(FormBuilder);
  private readonly identidad = inject(IdentidadStore);
  private readonly router = inject(Router);
  private readonly tiendaStore = inject(TiendaActualStore);

  protected readonly tienda = this.tiendaStore.tienda;
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly verClave = signal(false);

  protected readonly formulario = this.fb.nonNullable.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    clave: ['', [Validators.required]],
  });

  protected ingresar(): void {
    const tienda = this.tienda();
    if (this.formulario.invalid || !tienda) return;
    this.enviando.set(true);
    this.error.set(null);
    const { dni, clave } = this.formulario.getRawValue();
    this.identidad.iniciarSesionCliente(tienda.id, this.slug(), dni.trim(), clave).subscribe({
      next: () => this.router.navigate(['/t', this.slug(), 'mi-cuenta']),
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
