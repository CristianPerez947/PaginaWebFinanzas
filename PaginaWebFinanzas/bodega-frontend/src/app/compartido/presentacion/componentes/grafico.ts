import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  afterNextRender,
  effect,
  input,
  viewChild,
} from '@angular/core';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';

Chart.register(...registerables);

export interface SerieGrafico {
  nombre: string;
  valores: number[];
  color?: string;
}

const PALETA = ['#1f6f4a', '#d98a1c', '#2f6fb0', '#b3261e', '#7a5aa6', '#4e9a8c'];

/**
 * Envoltorio de Chart.js para barras, líneas y anillos, con la paleta de la aplicación.
 */
@Component({
  selector: 'app-grafico',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<div class="caja"><canvas #lienzo role="img" [attr.aria-label]="descripcion()"></canvas></div>`,
  styles: `
    .caja {
      position: relative;
      height: var(--alto, 260px);
      width: 100%;
    }
  `,
})
export class Grafico implements OnDestroy {
  readonly tipo = input<'bar' | 'line' | 'doughnut'>('bar');
  readonly etiquetas = input.required<string[]>();
  readonly series = input.required<SerieGrafico[]>();
  readonly horizontal = input(false);
  readonly apilado = input(false);
  readonly descripcion = input('Gráfico');
  readonly formatoMoneda = input(true);

  private readonly lienzo = viewChild.required<ElementRef<HTMLCanvasElement>>('lienzo');
  private grafico?: Chart;
  private listo = false;

  constructor() {
    afterNextRender(() => {
      this.listo = true;
      this.dibujar();
    });
    effect(() => {
      this.etiquetas();
      this.series();
      this.tipo();
      if (this.listo) this.dibujar();
    });
  }

  ngOnDestroy(): void {
    this.grafico?.destroy();
  }

  private dibujar(): void {
    this.grafico?.destroy();
    const tipo = this.tipo();
    const esAnillo = tipo === 'doughnut';
    const conMoneda = this.formatoMoneda();
    const configuracion: ChartConfiguration = {
      type: tipo as ChartType,
      data: {
        labels: this.etiquetas(),
        datasets: this.series().map((serie, indice) => ({
          label: serie.nombre,
          data: serie.valores,
          backgroundColor: esAnillo
            ? PALETA
            : tipo === 'line'
              ? (serie.color ?? PALETA[indice % PALETA.length]) + '33'
              : (serie.color ?? PALETA[indice % PALETA.length]),
          borderColor: esAnillo ? '#ffffff' : (serie.color ?? PALETA[indice % PALETA.length]),
          borderWidth: esAnillo ? 2 : tipo === 'line' ? 3 : 0,
          borderRadius: tipo === 'bar' ? 6 : 0,
          fill: tipo === 'line',
          tension: 0.3,
        })),
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        indexAxis: this.horizontal() ? 'y' : 'x',
        plugins: {
          legend: { display: esAnillo || this.series().length > 1, position: 'bottom' },
          tooltip: {
            callbacks: {
              label: (ctx) => {
                const valor = Number(esAnillo ? ctx.parsed : (this.horizontal() ? ctx.parsed.x : ctx.parsed.y));
                const texto = conMoneda ? `S/ ${valor.toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : `${valor}`;
                return `${ctx.dataset.label ? ctx.dataset.label + ': ' : ''}${texto}`;
              },
            },
          },
        },
        scales: esAnillo
          ? {}
          : {
              x: { stacked: this.apilado(), grid: { display: false } },
              y: { stacked: this.apilado(), beginAtZero: true, grid: { color: '#ecebe2' } },
            },
      },
    };
    this.grafico = new Chart(this.lienzo().nativeElement, configuracion);
  }
}
