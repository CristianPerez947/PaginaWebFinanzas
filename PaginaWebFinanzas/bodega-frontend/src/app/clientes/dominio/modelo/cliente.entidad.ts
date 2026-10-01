export type Moneda = 'PEN' | 'USD';
export type TipoTasa = 'EFECTIVA' | 'NOMINAL';
export type Capitalizacion = 'DIARIA' | 'MENSUAL';

/** Cliente con sus condiciones de crédito. Las tasas son anuales, en porcentaje (24 = 24 %). */
export interface Cliente {
  id: number;
  dni: string;
  nombres: string;
  apellidos: string;
  telefono: string;
  direccion: string | null;
  activo: boolean;
  moneda: Moneda;
  limiteCredito: number;
  maxMeses: number;
  diaCorte: number;
  horaCorte: string;
  diaPago: number;
  tipoTasa: TipoTasa;
  tasaCompensatoria: number;
  capitalizacion: Capitalizacion | null;
  tipoTasaMora: TipoTasa;
  tasaMora: number;
  capitalizacionMora: Capitalizacion | null;
}

export type GuardarCliente = Omit<Cliente, 'id' | 'activo'>;

export interface ClienteConClave {
  cliente: Cliente;
  clave: string;
}

export function nombreCompleto(cliente: Pick<Cliente, 'nombres' | 'apellidos'>): string {
  return `${cliente.nombres} ${cliente.apellidos}`.trim();
}
