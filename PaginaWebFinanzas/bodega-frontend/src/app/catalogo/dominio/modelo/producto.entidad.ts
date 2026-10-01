export interface Producto {
  id: number;
  nombre: string;
  categoria: string | null;
  marca: string | null;
  proveedor: string | null;
  descripcion: string | null;
  unidadMedida: string;
  imagen: string | null;
  precioContado: number;
  precioCredito: number;
  permiteCuotas: boolean;
  activo: boolean;
}

export type GuardarProducto = Omit<Producto, 'id' | 'activo'>;
