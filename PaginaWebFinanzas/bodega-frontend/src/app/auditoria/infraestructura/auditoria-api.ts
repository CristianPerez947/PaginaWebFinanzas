import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApi } from '../../compartido/infraestructura/base-api';
import { RegistroAuditoria } from '../dominio/modelo/auditoria.entidad';

@Injectable({ providedIn: 'root' })
export class AuditoriaApi extends BaseApi {
  listar(limite = 300): Observable<RegistroAuditoria[]> {
    return this.http.get<RegistroAuditoria[]>(this.url(`/auditoria?limite=${limite}`));
  }
}
