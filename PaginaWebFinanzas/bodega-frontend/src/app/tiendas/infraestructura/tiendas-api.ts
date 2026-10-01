import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApi } from '../../compartido/infraestructura/base-api';
import { ActualizarTienda, CrearTienda, RelojSistema, Tienda, TiendaPublica } from '../dominio/modelo/tienda.entidad';

@Injectable({ providedIn: 'root' })
export class TiendasApi extends BaseApi {
  listarPublicas(): Observable<TiendaPublica[]> {
    return this.http.get<TiendaPublica[]>(this.url('/publico/tiendas'));
  }

  obtenerPublica(slug: string): Observable<TiendaPublica> {
    return this.http.get<TiendaPublica>(this.url(`/publico/tiendas/${slug}`));
  }

  miTienda(): Observable<TiendaPublica> {
    return this.http.get<TiendaPublica>(this.url('/mi-tienda'));
  }

  listar(): Observable<Tienda[]> {
    return this.http.get<Tienda[]>(this.url('/tiendas'));
  }

  crear(tienda: CrearTienda): Observable<Tienda> {
    return this.http.post<Tienda>(this.url('/tiendas'), tienda);
  }

  actualizar(id: number, tienda: ActualizarTienda): Observable<Tienda> {
    return this.http.put<Tienda>(this.url(`/tiendas/${id}`), tienda);
  }

  cambiarEstado(id: number, activa: boolean): Observable<Tienda> {
    return this.http.patch<Tienda>(this.url(`/tiendas/${id}/estado`), { activa });
  }

  obtenerReloj(): Observable<RelojSistema> {
    return this.http.get<RelojSistema>(this.url('/reloj'));
  }

  simularReloj(fecha: string): Observable<RelojSistema> {
    return this.http.put<RelojSistema>(this.url('/reloj'), { fecha });
  }

  restaurarReloj(): Observable<RelojSistema> {
    return this.http.delete<RelojSistema>(this.url('/reloj'));
  }
}
