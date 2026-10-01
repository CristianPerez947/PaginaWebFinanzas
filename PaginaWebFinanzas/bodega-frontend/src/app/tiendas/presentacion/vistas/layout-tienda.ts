import { ChangeDetectionStrategy, Component, computed, effect, inject, input } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ImagenFallback } from '../../../compartido/presentacion/componentes/imagen-fallback';
import { IdentidadStore } from '../../../identidad/aplicacion/identidad-store';
import { TiendaActualStore } from '../../aplicacion/tienda-actual-store';

/**
 * Estructura de las páginas públicas de una bodega (/t/:slug): encabezado con sus datos y navegación
 * entre el catálogo y la cuenta del cliente.
 */
@Component({
  selector: 'app-layout-tienda',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatIconModule, ImagenFallback],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="cabecera">
      <div class="contenido">
        <a routerLink="/" class="volver" aria-label="Todas las bodegas"><mat-icon>arrow_back</mat-icon></a>
        @if (tienda(); as t) {
          <div class="logo"><app-imagen [src]="t.logo ? 'assets/logos/' + t.logo : null" icono="storefront" [alt]="t.nombre" /></div>
          <div class="datos">
            <h1>{{ t.nombre }}</h1>
            <span class="texto-suave">
              {{ t.direccion }}
              @if (t.telefono) { · <mat-icon inline>call</mat-icon> {{ t.telefono }} }
            </span>
          </div>
        }
        <span class="espaciador"></span>
        <nav>
          <a mat-button [routerLink]="['/t', slug()]" routerLinkActive="activo" [routerLinkActiveOptions]="{ exact: true }">
            <mat-icon>storefront</mat-icon> Productos
          </a>
          @if (esClienteDeEstaTienda()) {
            <a mat-button [routerLink]="['/t', slug(), 'mi-cuenta']" routerLinkActive="activo">
              <mat-icon>receipt_long</mat-icon> Mi cuenta
            </a>
            <button mat-stroked-button (click)="salir()"><mat-icon>logout</mat-icon> Salir</button>
          } @else {
            <a mat-flat-button color="primary" [routerLink]="['/t', slug(), 'ingresar']">
              <mat-icon>lock_open</mat-icon> Consultar mi cuenta
            </a>
          }
        </nav>
      </div>
    </header>

    @if (error()) {
      <div class="pagina"><div class="aviso-error">{{ error() }}</div></div>
    } @else if (tienda()) {
      <router-outlet />
    }
  `,
  styles: `
    .cabecera {
      background: #fff;
      border-bottom: 1px solid var(--borde);
      position: sticky;
      top: 0;
      z-index: 10;
    }
    .contenido {
      max-width: 1240px;
      margin: 0 auto;
      padding: 10px 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
    }
    .volver {
      color: var(--texto-suave);
      display: grid;
      place-items: center;
    }
    .logo {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      overflow: hidden;
      flex: none;
    }
    .datos h1 {
      font-size: 1.15rem;
    }
    .datos span {
      font-size: 0.85rem;
    }
    nav {
      display: flex;
      gap: 6px;
      align-items: center;
      flex-wrap: wrap;
    }
    a.activo {
      background: var(--verde-suave);
    }
  `,
})
export class LayoutTienda {
  readonly slug = input.required<string>();

  private readonly store = inject(TiendaActualStore);
  private readonly identidad = inject(IdentidadStore);

  protected readonly tienda = this.store.tienda;
  protected readonly error = this.store.error;
  protected readonly esClienteDeEstaTienda = computed(() => {
    const s = this.identidad.sesion();
    return s?.rol === 'CLIENTE' && s.slug === this.slug();
  });

  constructor() {
    effect(() => this.store.cargar(this.slug()));
  }

  protected salir(): void {
    this.identidad.cerrarSesion();
  }
}
