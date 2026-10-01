import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { AyudaCampo } from '../../../compartido/presentacion/componentes/ayuda-campo';
import { Tienda } from '../../dominio/modelo/tienda.entidad';
import { TiendasApi } from '../../infraestructura/tiendas-api';

/**
 * Alta y edición de una tienda junto con el usuario de su dueño/vendedor (el administrador del sistema
 * entrega estas credenciales al dueño).
 */
@Component({
  selector: 'app-tienda-form-dialogo',
  imports: [ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, AyudaCampo],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ tienda ? 'Editar tienda' : 'Nueva tienda' }}</h2>
    <mat-dialog-content>
      <form class="formulario-rejilla" [formGroup]="formulario" (ngSubmit)="guardar()" id="form-tienda">
        <div class="formulario-seccion">Datos de la tienda</div>
        <mat-form-field class="completo">
          <mat-label>Nombre comercial</mat-label>
          <input matInput formControlName="nombre" maxlength="120" />
          <app-ayuda matSuffix texto="Nombre con el que los vecinos conocen la bodega, por ejemplo: La Tiendita de Don Pepe." />
        </mat-form-field>
        @if (!tienda) {
          <mat-form-field class="completo">
            <mat-label>Identificador de la página (URL)</mat-label>
            <input matInput formControlName="slug" maxlength="80" placeholder="tiendita-don-pepe" />
            <app-ayuda matSuffix texto="Parte final de la dirección de la tienda: solo minúsculas, números y guiones, sin espacios. No se puede cambiar después." />
            @if (formulario.controls.slug.invalid && formulario.controls.slug.touched) { <mat-error>Solo minúsculas, números y guiones</mat-error> }
          </mat-form-field>
        }
        <mat-form-field>
          <mat-label>RUC (opcional)</mat-label>
          <input matInput formControlName="ruc" inputmode="numeric" maxlength="11" />
          <app-ayuda matSuffix texto="RUC de 11 dígitos, si la tienda lo tiene." />
          @if (formulario.controls.ruc.invalid) { <mat-error>Debe tener 11 dígitos</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Teléfono</mat-label>
          <input matInput formControlName="telefono" maxlength="20" />
          <app-ayuda matSuffix texto="Teléfono de contacto de la tienda." />
        </mat-form-field>
        <mat-form-field class="completo">
          <mat-label>Dirección</mat-label>
          <input matInput formControlName="direccion" maxlength="200" />
          <app-ayuda matSuffix texto="Dirección de la bodega." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Logotipo (nombre del archivo)</mat-label>
          <input matInput formControlName="logo" maxlength="120" placeholder="logo-don-pepe.png" />
          <app-ayuda matSuffix texto="Archivo que colocaste en public/assets/logos, por ejemplo logo-don-pepe.png. Si no existe se muestra un ícono." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Tipo de cambio (S/ por US$)</mat-label>
          <input matInput type="number" step="0.0001" min="0.0001" formControlName="tipoCambioUsd" />
          <app-ayuda matSuffix texto="Se usa para convertir los precios en soles cuando un cliente tiene su crédito en dólares." />
        </mat-form-field>

        <div class="formulario-seccion">Dueño y acceso del vendedor</div>
        <mat-form-field class="completo">
          <mat-label>Nombre del dueño</mat-label>
          <input matInput formControlName="nombreDueno" maxlength="120" />
          <app-ayuda matSuffix texto="Nombre completo del dueño o encargado de la bodega." />
        </mat-form-field>
        @if (!tienda) {
          <mat-form-field>
            <mat-label>Usuario del vendedor</mat-label>
            <input matInput formControlName="usuarioVendedor" maxlength="80" autocomplete="off" />
            <app-ayuda matSuffix texto="Usuario con el que el vendedor ingresará al sistema. Debe ser único." />
          </mat-form-field>
          <mat-form-field>
            <mat-label>Clave inicial</mat-label>
            <input matInput type="password" formControlName="claveVendedor" autocomplete="new-password" />
            <app-ayuda matSuffix texto="Mínimo 6 caracteres. Entrégasela al dueño; se guarda cifrada." />
            @if (formulario.controls.claveVendedor.invalid && formulario.controls.claveVendedor.touched) { <mat-error>Mínimo 6 caracteres</mat-error> }
          </mat-form-field>
        } @else {
          <p class="completo texto-suave">Usuario del vendedor: <strong>{{ tienda.usuarioVendedor }}</strong></p>
        }
      </form>
      @if (error()) { <div class="aviso-error">{{ error() }}</div> }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-flat-button color="primary" type="submit" form="form-tienda" [disabled]="formulario.invalid || guardando()">
        {{ guardando() ? 'Guardando…' : 'Guardar' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class TiendaFormDialogo {
  protected readonly tienda = inject<Tienda | null>(MAT_DIALOG_DATA, { optional: true });
  private readonly ref = inject<MatDialogRef<TiendaFormDialogo, Tienda>>(MatDialogRef);
  private readonly api = inject(TiendasApi);
  private readonly fb = inject(FormBuilder);

  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = this.fb.nonNullable.group({
    nombre: [this.tienda?.nombre ?? '', Validators.required],
    slug: [this.tienda?.slug ?? '', this.tienda ? [] : [Validators.required, Validators.pattern(/^[a-z0-9]+(-[a-z0-9]+)*$/)]],
    ruc: [this.tienda?.ruc ?? '', [Validators.pattern(/^(\d{11})?$/)]],
    telefono: [this.tienda?.telefono ?? ''],
    direccion: [this.tienda?.direccion ?? ''],
    logo: [this.tienda?.logo ?? ''],
    tipoCambioUsd: [this.tienda?.tipoCambioUsd ?? 3.75, [Validators.required, Validators.min(0.0001)]],
    nombreDueno: [this.tienda?.nombreDueno ?? '', Validators.required],
    usuarioVendedor: ['', this.tienda ? [] : [Validators.required]],
    claveVendedor: ['', this.tienda ? [] : [Validators.required, Validators.minLength(6)]],
  });

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const v = this.formulario.getRawValue();
    const comunes = {
      nombre: v.nombre.trim(),
      logo: v.logo.trim() || null,
      ruc: v.ruc.trim() || null,
      direccion: v.direccion.trim() || null,
      telefono: v.telefono.trim() || null,
      tipoCambioUsd: Number(v.tipoCambioUsd),
      nombreDueno: v.nombreDueno.trim(),
    };
    this.guardando.set(true);
    this.error.set(null);
    const peticion = this.tienda
      ? this.api.actualizar(this.tienda.id, comunes)
      : this.api.crear({ ...comunes, slug: v.slug.trim(), usuarioVendedor: v.usuarioVendedor.trim(), claveVendedor: v.claveVendedor });
    peticion.subscribe({
      next: (t) => this.ref.close(t),
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.guardando.set(false);
      },
    });
  }
}
