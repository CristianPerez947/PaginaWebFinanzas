import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Ícono de ayuda para cada campo de los formularios: al pasar el cursor, enfocar con el teclado o tocar,
 * muestra qué debe ingresarse y en qué formato (requisito de ayuda por campo).
 */
@Component({
  selector: 'app-ayuda',
  imports: [MatIconModule, MatTooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-icon
      class="ayuda"
      #tip="matTooltip"
      [matTooltip]="texto()"
      matTooltipPosition="above"
      matTooltipClass="tooltip-ayuda"
      tabindex="0"
      role="img"
      [attr.aria-label]="'Ayuda: ' + texto()"
      (click)="$event.stopPropagation(); tip.toggle()"
      (keydown.enter)="tip.toggle()"
      >help_outline</mat-icon
    >
  `,
  styles: `
    .ayuda {
      cursor: help;
      color: #5d6b63;
      font-size: 20px;
      width: 20px;
      height: 20px;
      margin-right: 10px;
      vertical-align: middle;
    }
    .ayuda:hover,
    .ayuda:focus-visible {
      color: #1f6f4a;
      outline: none;
    }
  `,
})
export class AyudaCampo {
  readonly texto = input.required<string>();
}
