import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApi } from '../../compartido/infraestructura/base-api';
import {
  DeudaCliente,
  RegistrarVenta,
  ResumenCondonacion,
  ResumenPago,
  Venta,
} from '../dominio/modelo/credito.entidad';

@Injectable({ providedIn: 'root' })
export class CreditoApi extends BaseApi {
  simularVenta(venta: RegistrarVenta): Observable<Venta> {
    return this.http.post<Venta>(this.url('/ventas/simular'), venta);
  }

  registrarVenta(venta: RegistrarVenta): Observable<Venta> {
    return this.http.post<Venta>(this.url('/ventas'), venta);
  }

  listarVentas(clienteId?: number): Observable<Venta[]> {
    const parametros = clienteId ? `?clienteId=${clienteId}` : '';
    return this.http.get<Venta[]>(this.url(`/ventas${parametros}`));
  }

  deudaDeCliente(clienteId: number): Observable<DeudaCliente> {
    return this.http.get<DeudaCliente>(this.url(`/clientes/${clienteId}/deuda`));
  }

  pagarCiclo(clienteId: number, fechaPagoCiclo: string, fechaPago: string): Observable<ResumenPago> {
    return this.http.post<ResumenPago>(this.url(`/clientes/${clienteId}/pagos-ciclo`), { fechaPagoCiclo, fechaPago });
  }

  pagarCuota(obligacionId: number, numeroCuota: number, fechaPago: string): Observable<ResumenPago> {
    return this.http.post<ResumenPago>(this.url(`/obligaciones/${obligacionId}/cuotas/${numeroCuota}/pago`), { fechaPago });
  }

  condonarObligacion(obligacionId: number, motivo: string): Observable<ResumenCondonacion> {
    return this.http.post<ResumenCondonacion>(this.url(`/obligaciones/${obligacionId}/condonacion`), { motivo });
  }

  condonarDeudaCliente(clienteId: number, motivo: string): Observable<ResumenCondonacion> {
    return this.http.post<ResumenCondonacion>(this.url(`/clientes/${clienteId}/condonacion`), { motivo });
  }

  miDeuda(): Observable<DeudaCliente> {
    return this.http.get<DeudaCliente>(this.url('/mi-cuenta/deuda'));
  }

  misCompras(): Observable<Venta[]> {
    return this.http.get<Venta[]>(this.url('/mi-cuenta/compras'));
  }
}
