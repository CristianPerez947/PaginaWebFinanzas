import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApi } from '../../compartido/infraestructura/base-api';
import { GuardarProducto, Producto } from '../dominio/modelo/producto.entidad';

@Injectable({ providedIn: 'root' })
export class CatalogoApi extends BaseApi {
  catalogoPublico(slug: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.url(`/publico/tiendas/${slug}/productos`));
  }

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.url('/productos'));
  }

  crear(producto: GuardarProducto): Observable<Producto> {
    return this.http.post<Producto>(this.url('/productos'), producto);
  }

  actualizar(id: number, producto: GuardarProducto): Observable<Producto> {
    return this.http.put<Producto>(this.url(`/productos/${id}`), producto);
  }

  cambiarEstado(id: number, activo: boolean): Observable<Producto> {
    return this.http.patch<Producto>(this.url(`/productos/${id}/estado`), { activo });
  }
}
