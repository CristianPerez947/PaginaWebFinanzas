export type TipoLinea = 'COMPRA' | 'INTERES_COMPENSATORIO' | 'CUOTA' | 'MORA' | 'COMPRA_EN_CUOTAS';
export type TipoMensaje = 'ESTADO_CUENTA' | 'RECORDATORIO_VENCIMIENTO' | 'PAGO_RECIBIDO';

export interface LineaEstadoCuenta {
  tipo: TipoLinea;
  fecha: string;
  descripcion: string;
  monto: number;
  incluidaEnTotal: boolean;
}

export interface EstadoCuenta {
  id: number;
  clienteId: number;
  nombreCliente: string;
  dniCliente: string;
  moneda: string;
  fechaCorte: string;
  fechaPago: string;
  fechaGeneracion: string;
  totalCompras: number;
  totalIntereses: number;
  totalCuotas: number;
  totalMora: number;
  totalAPagar: number;
  estado: 'PENDIENTE' | 'PAGADO';
  lineas: LineaEstadoCuenta[];
}

export interface MensajeWhatsApp {
  id: number;
  clienteId: number;
  nombreCliente: string;
  telefono: string;
  tipo: TipoMensaje;
  contenido: string;
  fechaEnvio: string;
  estadoEnvio: string;
}

export const ETIQUETA_TIPO_MENSAJE: Record<TipoMensaje, string> = {
  ESTADO_CUENTA: 'Estado de cuenta',
  RECORDATORIO_VENCIMIENTO: 'Recordatorio de vencimiento',
  PAGO_RECIBIDO: 'Pago recibido',
};
