/** Tienda tal como la ve el administrador del sistema. */
export interface Tienda {
  id: number;
  nombre: string;
  slug: string;
  logo: string | null;
  ruc: string | null;
  direccion: string | null;
  telefono: string | null;
  tipoCambioUsd: number;
  activa: boolean;
  nombreDueno: string;
  usuarioVendedor: string | null;
}

/** Datos públicos de una tienda (página de la bodega). */
export interface TiendaPublica {
  id: number;
  nombre: string;
  slug: string;
  logo: string | null;
  direccion: string | null;
  telefono: string | null;
  tipoCambioUsd: number;
}

export interface CrearTienda {
  nombre: string;
  slug: string;
  logo: string | null;
  ruc: string | null;
  direccion: string | null;
  telefono: string | null;
  tipoCambioUsd: number;
  nombreDueno: string;
  usuarioVendedor: string;
  claveVendedor: string;
}

export type ActualizarTienda = Omit<CrearTienda, 'slug' | 'usuarioVendedor' | 'claveVendedor'>;

export interface RelojSistema {
  ahora: string;
  simulado: boolean;
}
