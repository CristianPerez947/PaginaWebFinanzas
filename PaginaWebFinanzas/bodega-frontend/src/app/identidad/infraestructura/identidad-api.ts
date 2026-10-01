import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApi } from '../../compartido/infraestructura/base-api';
import { Sesion } from '../dominio/modelo/sesion.entidad';

@Injectable({ providedIn: 'root' })
export class IdentidadApi extends BaseApi {
  iniciarSesion(usuario: string, clave: string): Observable<Sesion> {
    return this.http.post<Sesion>(this.url('/autenticacion/iniciar-sesion'), { usuario, clave });
  }

  iniciarSesionCliente(tiendaId: number, dni: string, clave: string): Observable<Sesion> {
    return this.http.post<Sesion>(this.url('/autenticacion/cliente'), { tiendaId, dni, clave });
  }
}
