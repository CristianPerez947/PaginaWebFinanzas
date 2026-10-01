import { ChangeDetectionStrategy, Component, effect, input, signal, untracked } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * Muestra una imagen de la carpeta assets; si el archivo no existe, muestra un ícono en su lugar.
 */
@Component({
  selector: 'app-imagen',
  imports: [MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (src() && !fallo()) {
      <img [src]="src()" [alt]="alt()" (error)="fallo.set(true)" loading="lazy" />
    } @else {
      <div class="marcador"><mat-icon>{{ icono() }}</mat-icon></div>
    }
  `,
  styles: `
    :host {
      display: block;
      width: 100%;
      height: 100%;
    }
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
    .marcador {
      width: 100%;
      height: 100%;
      display: grid;
      place-items: center;
      background: linear-gradient(135deg, #e3f1ea, #fdf0d9);
      color: #6f8f7d;
    }
    .marcador mat-icon {
      font-size: 42px;
      width: 42px;
      height: 42px;
    }
  `,
})
export class ImagenFallback {
  readonly src = input<string | null>(null);
  readonly alt = input('');
  readonly icono = input('shopping_basket');
  protected readonly fallo = signal(false);

  constructor() {
    effect(() => {
      this.src();
      untracked(() => this.fallo.set(false));
    });
  }
}
