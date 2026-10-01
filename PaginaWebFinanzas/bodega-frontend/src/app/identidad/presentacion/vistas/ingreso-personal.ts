import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ConfiguracionApp } from '../../../compartido/infraestructura/configuracion-app';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { AyudaCampo } from '../../../compartido/presentacion/componentes/ayuda-campo';
import { IdentidadStore } from '../../aplicacion/identidad-store';

/**
 * Ingreso del vendedor de una bodega y del administrador del sistema.
 */
@Component({
  selector: 'app-ingreso-personal',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    AyudaCampo,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="fondo">
      <div class="columna">
        <a routerLink="/" class="marca"><mat-icon>menu_book</mat-icon> Cuaderno de Fiado Digital</a>
        <form class="tarjeta" [formGroup]="formulario" (ngSubmit)="ingresar()">
          <h2>Ingreso del personal</h2>
          <p class="texto-suave">Para vendedores de una bodega y para el administrador del sistema.</p>

          <mat-form-field>
            <mat-label>Usuario</mat-label>
            <input matInput formControlName="usuario" autocomplete="username" />
            <app-ayuda matSuffix texto="El usuario que le asignó el administrador del sistema al crear su bodega." />
          </mat-form-field>

          <mat-form-field>
            <mat-label>Clave</mat-label>
            <input matInput formControlName="clave" [type]="verClave() ? 'text' : 'password'" autocomplete="current-password" />
            <button mat-icon-button matSuffix type="button" (click)="verClave.set(!verClave())" aria-label="Mostrar u ocultar la clave">
              <mat-icon>{{ verClave() ? 'visibility_off' : 'visibility' }}</mat-icon>
            </button>
          </mat-form-field>

          @if (error()) {
            <div class="aviso-error">{{ error() }}</div>
          }

          <button mat-flat-button color="primary" type="submit" [disabled]="formulario.invalid || enviando()">
            {{ enviando() ? 'Ingresando…' : 'Ingresar' }}
          </button>
        </form>

        @if (config.mostrarDemo()) {
          <mat-accordion>
            <mat-expansion-panel>
              <mat-expansion-panel-header>
                <mat-panel-title><mat-icon>science</mat-icon>&nbsp; Usuarios de demostración</mat-panel-title>
              </mat-expansion-panel-header>
              <table class="tabla">
                <tr><th>Rol</th><th>Usuario</th><th>Clave</th></tr>
                <tr><td>Administrador</td><td>admin</td><td>admin123</td></tr>
                <tr><td>Vendedor — Don Pepe</td><td>pepe</td><td>pepe123</td></tr>
                <tr><td>Vendedor — Doña Rosa</td><td>rosa</td><td>rosa123</td></tr>
              </table>
              <p class="texto-suave">Los clientes de demostración entran desde la página de su bodega con la clave 123456.</p>
            </mat-expansion-panel>
          </mat-accordion>
        }
      </div>
    </div>
  `,
  styles: `
    .fondo {
      min-height: 100vh;
      display: grid;
      place-items: center;
      padding: 24px 16px;
      background: linear-gradient(160deg, #e3f1ea, #f6f5f0 55%);
    }
    .columna {
      width: 100%;
      max-width: 440px;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .marca {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 800;
      color: var(--verde);
      text-decoration: none;
      justify-content: center;
      font-size: 1.15rem;
    }
    form {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }
    p {
      margin: 0;
    }
  `,
})
export class IngresoPersonal {
  private readonly fb = inject(FormBuilder);
  private readonly identidad = inject(IdentidadStore);
  private readonly router = inject(Router);
  protected readonly config = inject(ConfiguracionApp);

  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly verClave = signal(false);

  protected readonly formulario = this.fb.nonNullable.group({
    usuario: ['', Validators.required],
    clave: ['', Validators.required],
  });

  protected ingresar(): void {
    if (this.formulario.invalid) return;
    this.enviando.set(true);
    this.error.set(null);
    const { usuario, clave } = this.formulario.getRawValue();
    this.identidad.iniciarSesionPersonal(usuario.trim(), clave).subscribe({
      next: () => this.router.navigateByUrl(this.identidad.rutaInicio()),
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
