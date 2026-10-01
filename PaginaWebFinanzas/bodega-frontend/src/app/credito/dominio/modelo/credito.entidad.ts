export type Modalidad = 'CONTADO' | 'FIN_DE_MES' | 'CUOTAS';
export type EstadoDeuda = 'PENDIENTE' | 'PAGADA' | 'CONDONADA';

export interface Cuota {
  numero: number;
  fechaVencimiento: string;
  cuota: number;
  interes: number;
  amortizacion: number;
  saldo: number;
  estado: EstadoDeuda;
  fechaPagoReal: string | null;
  diasMora: number;
  mora: number;
  totalAPagar: number;
}

export interface Pago {
  numeroCuota: number | null;
  fechaPago: string;
  montoMora: number;
  montoInteres: number;
  montoCapital: number;
  total: number;
}

export interface Obligacion {
  id: number | null;
  ventaId: number | null;
  modalidad: Modalidad;
  moneda: string;
  capital: number;
  interesCompensatorio: number;
  fechaCompra: string;
  fechaCorte: string;
  fechaPago: string;
  diasGracia: number;
  numCuotas: number;
  teaCompensatoria: number;
  teaMora: number;
  estado: EstadoDeuda;
  enMora: boolean;
  diasMora: number;
  mora: number;
  importeExigible: number;
  cuotas: Cuota[];
  pagos: Pago[];
  motivoCondonacion: string | null;
  fechaCondonacion: string | null;
}

export interface LineaVenta {
  productoId: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  modalidad: Modalidad;
  numCuotas: number | null;
}

export interface Venta {
  id: number | null;
  clienteId: number;
  fechaHora: string;
  moneda: string;
  tipoCambioAplicado: number | null;
  total: number;
  lineas: LineaVenta[];
  obligaciones: Obligacion[] | null;
}

export interface LineaVentaSolicitud {
  productoId: number;
  cantidad: number;
  modalidad: Modalidad;
  numCuotas: number | null;
}

export interface RegistrarVenta {
  clienteId: number;
  lineas: LineaVentaSolicitud[];
}

export interface DeudaCliente {
  clienteId: number;
  moneda: string;
  limiteCredito: number;
  capitalPendiente: number;
  disponible: number;
  enMora: boolean;
  totalVencido: number;
  fechaConsulta: string;
  obligaciones: Obligacion[];
}

export interface ResumenPago {
  obligaciones: number;
  fechaPago: string;
  mora: number;
  interes: number;
  capital: number;
  total: number;
}

export interface ResumenCondonacion {
  obligaciones: number;
  montoCondonado: number;
}

export const ETIQUETA_MODALIDAD: Record<Modalidad, string> = {
  CONTADO: 'Al contado',
  FIN_DE_MES: 'A fin de mes',
  CUOTAS: 'En cuotas',
};
