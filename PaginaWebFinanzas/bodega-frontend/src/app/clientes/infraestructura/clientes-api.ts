import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApi } from '../../compartido/infraestructura/base-api';
import { Cliente, ClienteConClave, GuardarCliente } from '../dominio/modelo/cliente.entidad';

@Injectable({ providedIn: 'root' })
export class ClientesApi extends BaseApi {
  listar(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.url('/clientes'));
  }

  obtener(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(this.url(`/clientes/${id}`));
  }

  crear(cliente: GuardarCliente): Observable<ClienteConClave> {
    return this.http.post<ClienteConClave>(this.url('/clientes'), cliente);
  }

  actualizar(id: number, cliente: GuardarCliente): Observable<Cliente> {
    return this.http.put<Cliente>(this.url(`/clientes/${id}`), cliente);
  }

  cambiarEstado(id: number, activo: boolean): Observable<Cliente> {
    return this.http.patch<Cliente>(this.url(`/clientes/${id}/estado`), { activo });
  }

  regenerarClave(id: number): Observable<ClienteConClave> {
    return this.http.post<ClienteConClave>(this.url(`/clientes/${id}/regenerar-clave`), {});
  }

  miPerfil(): Observable<Cliente> {
    return this.http.get<Cliente>(this.url('/mi-cuenta/perfil'));
  }
}
