import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApi } from '../../compartido/infraestructura/base-api';
import { EstadoCuenta, MensajeWhatsApp } from '../dominio/modelo/estado-cuenta.entidad';

@Injectable({ providedIn: 'root' })
export class EstadoCuentaApi extends BaseApi {
  listar(clienteId?: number): Observable<EstadoCuenta[]> {
    const parametros = clienteId ? `?clienteId=${clienteId}` : '';
    return this.http.get<EstadoCuenta[]>(this.url(`/estados-cuenta${parametros}`));
  }

  generar(clienteId: number, fechaCorte?: string): Observable<EstadoCuenta> {
    return this.http.post<EstadoCuenta>(this.url('/estados-cuenta/generar'), { clienteId, fechaCorte: fechaCorte ?? null });
  }

  generarCortes(): Observable<{ generados: number }> {
    return this.http.post<{ generados: number }>(this.url('/estados-cuenta/generar-cortes'), {});
  }

  pdf(id: number): Observable<Blob> {
    return this.http.get(this.url(`/estados-cuenta/${id}/pdf`), { responseType: 'blob' });
  }

  mensajes(limite = 100): Observable<MensajeWhatsApp[]> {
    return this.http.get<MensajeWhatsApp[]>(this.url(`/mensajes-whatsapp?limite=${limite}`));
  }

  misEstadosCuenta(): Observable<EstadoCuenta[]> {
    return this.http.get<EstadoCuenta[]>(this.url('/mi-cuenta/estados-cuenta'));
  }

  miPdf(id: number): Observable<Blob> {
    return this.http.get(this.url(`/mi-cuenta/estados-cuenta/${id}/pdf`), { responseType: 'blob' });
  }
}
