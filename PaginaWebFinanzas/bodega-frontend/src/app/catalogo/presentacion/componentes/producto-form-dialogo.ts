import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { AyudaCampo } from '../../../compartido/presentacion/componentes/ayuda-campo';
import { GuardarProducto, Producto } from '../../dominio/modelo/producto.entidad';
import { CatalogoApi } from '../../infraestructura/catalogo-api';

/**
 * Alta y edición de un producto o servicio con sus precios al contado y a crédito.
 */
@Component({
  selector: 'app-producto-form-dialogo',
  imports: [ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSlideToggleModule, AyudaCampo],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ producto ? 'Editar producto' : 'Nuevo producto' }}</h2>
    <mat-dialog-content>
      <form class="formulario-rejilla" [formGroup]="formulario" (ngSubmit)="guardar()" id="form-producto">
        <mat-form-field class="completo">
          <mat-label>Nombre del producto</mat-label>
          <input matInput formControlName="nombre" maxlength="150" />
          <app-ayuda matSuffix texto="Cómo lo conocen tus clientes, con su presentación (por ejemplo: Arroz Costeño 1 kg)." />
          @if (formulario.controls.nombre.invalid && formulario.controls.nombre.touched) { <mat-error>El nombre es obligatorio</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Categoría</mat-label>
          <input matInput formControlName="categoria" maxlength="80" />
          <app-ayuda matSuffix texto="Grupo del producto para ordenar el catálogo: Abarrotes, Lácteos, Bebidas, Limpieza…" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Unidad de medida</mat-label>
          <input matInput formControlName="unidadMedida" maxlength="30" />
          <app-ayuda matSuffix texto="Cómo se vende: kg, litro, unidad, paquete, docena…" />
          @if (formulario.controls.unidadMedida.invalid && formulario.controls.unidadMedida.touched) { <mat-error>Obligatoria</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Marca</mat-label>
          <input matInput formControlName="marca" maxlength="80" />
          <app-ayuda matSuffix texto="Marca comercial (opcional)." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Proveedor</mat-label>
          <input matInput formControlName="proveedor" maxlength="120" />
          <app-ayuda matSuffix texto="Quién te lo provee (opcional)." />
        </mat-form-field>
        <mat-form-field class="completo">
          <mat-label>Descripción</mat-label>
          <input matInput formControlName="descripcion" maxlength="400" />
          <app-ayuda matSuffix texto="Detalle breve del producto (opcional)." />
        </mat-form-field>
        <mat-form-field class="completo">
          <mat-label>Imagen (nombre del archivo)</mat-label>
          <input matInput formControlName="imagen" maxlength="150" placeholder="arroz-costeno.jpg" />
          <app-ayuda matSuffix texto="Nombre del archivo que colocaste en la carpeta public/assets/productos, por ejemplo arroz-costeno.jpg. Si no existe, se muestra un ícono." />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Precio al contado (S/)</mat-label>
          <input matInput type="number" step="0.01" min="0.01" formControlName="precioContado" />
          <app-ayuda matSuffix texto="Precio si el cliente paga en el momento." />
          @if (formulario.controls.precioContado.invalid && formulario.controls.precioContado.touched) { <mat-error>Debe ser mayor que cero</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Precio de lista a crédito (S/)</mat-label>
          <input matInput type="number" step="0.01" min="0.01" formControlName="precioCredito" />
          <app-ayuda matSuffix texto="Precio cuando se vende fiado. No puede ser menor que el contado. Ojo: además se cobran los intereses del crédito, así que solo súbelo si quieres un recargo adicional." />
        </mat-form-field>
        <div class="completo">
          <mat-slide-toggle formControlName="permiteCuotas">Se puede vender en cuotas mensuales</mat-slide-toggle>
          <app-ayuda texto="Si está activo, el vendedor puede elegir pagarlo en varios meses (método francés). Si no, solo se vende al contado o a fin de mes." />
        </div>
      </form>
      @if (formulario.errors?.['creditoMenor']) { <div class="aviso-error">El precio a crédito no puede ser menor que el contado.</div> }
      @if (error()) { <div class="aviso-error">{{ error() }}</div> }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-flat-button color="primary" type="submit" form="form-producto" [disabled]="formulario.invalid || guardando()">
        {{ guardando() ? 'Guardando…' : 'Guardar' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ProductoFormDialogo {
  protected readonly producto = inject<Producto | null>(MAT_DIALOG_DATA, { optional: true });
  private readonly ref = inject<MatDialogRef<ProductoFormDialogo, Producto>>(MatDialogRef);
  private readonly api = inject(CatalogoApi);
  private readonly fb = inject(FormBuilder);

  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = this.fb.nonNullable.group(
    {
      nombre: [this.producto?.nombre ?? '', Validators.required],
      categoria: [this.producto?.categoria ?? ''],
      marca: [this.producto?.marca ?? ''],
      proveedor: [this.producto?.proveedor ?? ''],
      descripcion: [this.producto?.descripcion ?? ''],
      unidadMedida: [this.producto?.unidadMedida ?? 'unidad', Validators.required],
      imagen: [this.producto?.imagen ?? ''],
      precioContado: [this.producto?.precioContado ?? 0, [Validators.required, Validators.min(0.01)]],
      precioCredito: [this.producto?.precioCredito ?? 0, [Validators.required, Validators.min(0.01)]],
      permiteCuotas: [this.producto?.permiteCuotas ?? false],
    },
    {
      validators: (grupo) => {
        const contado = Number(grupo.get('precioContado')?.value);
        const credito = Number(grupo.get('precioCredito')?.value);
        return credito < contado ? { creditoMenor: true } : null;
      },
    },
  );

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const v = this.formulario.getRawValue();
    const datos: GuardarProducto = {
      nombre: v.nombre.trim(),
      categoria: v.categoria.trim() || null,
      marca: v.marca.trim() || null,
      proveedor: v.proveedor.trim() || null,
      descripcion: v.descripcion.trim() || null,
      unidadMedida: v.unidadMedida.trim(),
      imagen: v.imagen.trim() || null,
      precioContado: Number(v.precioContado),
      precioCredito: Number(v.precioCredito),
      permiteCuotas: v.permiteCuotas,
    };
    this.guardando.set(true);
    this.error.set(null);
    const peticion = this.producto ? this.api.actualizar(this.producto.id, datos) : this.api.crear(datos);
    peticion.subscribe({
      next: (p) => this.ref.close(p),
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.guardando.set(false);
      },
    });
  }
}
