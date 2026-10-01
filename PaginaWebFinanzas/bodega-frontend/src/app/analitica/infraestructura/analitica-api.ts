import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApi } from '../../compartido/infraestructura/base-api';
import {
  FilaDeudor,
  FilaInteresMensual,
  FilaMoroso,
  FilaProductoVendido,
  PanelVendedor,
  ResumenCartera,
} from '../dominio/modelo/analitica.entidad';

@Injectable({ providedIn: 'root' })
export class AnaliticaApi extends BaseApi {
  panel(): Observable<PanelVendedor> {
    return this.http.get<PanelVendedor>(this.url('/analitica/panel'));
  }

  deudores(): Observable<FilaDeudor[]> {
    return this.http.get<FilaDeudor[]>(this.url('/analitica/reportes/deudores'));
  }

  morosos(): Observable<FilaMoroso[]> {
    return this.http.get<FilaMoroso[]>(this.url('/analitica/reportes/morosos'));
  }

  ventasPorProducto(): Observable<FilaProductoVendido[]> {
    return this.http.get<FilaProductoVendido[]>(this.url('/analitica/reportes/ventas-por-producto'));
  }

  intereses(): Observable<FilaInteresMensual[]> {
    return this.http.get<FilaInteresMensual[]>(this.url('/analitica/reportes/intereses'));
  }

  cartera(): Observable<ResumenCartera> {
    return this.http.get<ResumenCartera>(this.url('/analitica/reportes/cartera'));
  }
}
