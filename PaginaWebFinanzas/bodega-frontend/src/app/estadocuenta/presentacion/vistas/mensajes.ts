import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { mensajeDeError } from '../../../compartido/infraestructura/base-api';
import { FechaHoraPipe } from '../../../compartido/infraestructura/formato';
import { ETIQUETA_TIPO_MENSAJE, MensajeWhatsApp } from '../../dominio/modelo/estado-cuenta.entidad';
import { EstadoCuentaApi } from '../../infraestructura/estado-cuenta-api';

/**
 * Bandeja de mensajes de WhatsApp enviados a los clientes. En esta versión el envío es simulado.
 */
@Component({
  selector: 'app-mensajes',
  imports: [MatIconModule, FechaHoraPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="pagina-encabezado">
        <div>
          <h1>Mensajes de WhatsApp</h1>
          <p>Avisos automáticos a tus clientes: estado de cuenta, recordatorio de vencimiento y pago recibido.</p>
        </div>
      </div>
      <div class="aviso-info"><mat-icon inline>info</mat-icon> Versión de demostración: los mensajes se guardan y se muestran aquí como “enviados”, pero no salen realmente por WhatsApp.</div>
      @if (error()) { <div class="aviso-error">{{ error() }}</div> }

      <div class="chat">
        @for (m of mensajes(); track m.id) {
          <article class="globo">
            <header>
              <strong>{{ m.nombreCliente }}</strong>
              <span class="texto-suave">{{ m.telefono }}</span>
              <span class="etiqueta-estado neutra">{{ etiqueta[m.tipo] }}</span>
              <span class="espaciador"></span>
              <span class="texto-suave">{{ m.fechaEnvio | fechaHora }}</span>
            </header>
            <p>{{ m.contenido }}</p>
            <footer><mat-icon inline>done_all</mat-icon> {{ m.estadoEnvio }}</footer>
          </article>
        } @empty {
          <div class="vacio tarjeta"><mat-icon>chat</mat-icon><p>Aún no se han enviado mensajes.</p></div>
        }
      </div>
    </div>
  `,
  styles: `
    .chat {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-top: 14px;
      max-width: 820px;
    }
    .globo {
      background: #e7f6dd;
      border: 1px solid #cfe7c2;
      border-radius: 4px 16px 16px 16px;
      padding: 12px 16px;
    }
    .globo header {
      display: flex;
      gap: 10px;
      align-items: center;
      flex-wrap: wrap;
    }
    .globo p {
      margin: 8px 0;
      line-height: 1.45;
    }
    .globo footer {
      color: #2a7a4a;
      font-size: 0.8rem;
      font-weight: 700;
    }
  `,
})
export class Mensajes {
  private readonly api = inject(EstadoCuentaApi);
  protected readonly mensajes = signal<MensajeWhatsApp[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly etiqueta = ETIQUETA_TIPO_MENSAJE;

  constructor() {
    this.api.mensajes(100).subscribe({
      next: (m) => this.mensajes.set(m),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
