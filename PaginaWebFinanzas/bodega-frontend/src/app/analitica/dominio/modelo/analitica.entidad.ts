export interface PuntoSerie {
  etiqueta: string;
  valor: number;
}

export interface FilaDeudor {
  clienteId: number;
  cliente: string;
  dni: string;
  telefono: string;
  capitalPendiente: number;
  limiteCredito: number;
  importeExigible: number;
  enMora: boolean;
}

export interface FilaMoroso {
  clienteId: number;
  cliente: string;
  telefono: string;
  diasMoraMaximo: number;
  importeVencido: number;
  moraAcumulada: number;
}

export interface FilaProductoVendido {
  producto: string;
  cantidad: number;
  monto: number;
}

export interface FilaInteresMensual {
  mes: string;
  interesesCompensatorios: number;
  interesesMora: number;
  total: number;
}

export interface ProximoVencimiento {
  clienteId: number;
  cliente: string;
  fecha: string;
  importe: number;
  concepto: string;
}

export interface ResumenCartera {
  capitalPendiente: number;
  deudaVencida: number;
  interesesPorCobrar: number;
  interesesCobrados: number;
  moraCobrada: number;
  totalCobrado: number;
  porModalidad: PuntoSerie[];
  porEstado: PuntoSerie[];
}

export interface PanelVendedor {
  fecha: string;
  clientesActivos: number;
  clientesConDeuda: number;
  clientesEnMora: number;
  carteraPendiente: number;
  deudaVencida: number;
  interesesPorCobrar: number;
  totalCobrado: number;
  ventasDelMes: number;
  ventasPorMes: PuntoSerie[];
  cobrosPorMes: PuntoSerie[];
  ventasPorModalidad: PuntoSerie[];
  topProductos: FilaProductoVendido[];
  topDeudores: FilaDeudor[];
  morosos: FilaMoroso[];
  proximosVencimientos: ProximoVencimiento[];
}
