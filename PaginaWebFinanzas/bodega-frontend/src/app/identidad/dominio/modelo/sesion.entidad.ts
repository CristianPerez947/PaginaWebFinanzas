export type RolUsuario = 'ADMIN_SISTEMA' | 'VENDEDOR' | 'CLIENTE';

/** Sesión iniciada: quién es el usuario, su rol, su tienda y el token de acceso. */
export interface Sesion {
  id: number;
  usuario: string;
  rol: RolUsuario;
  tiendaId: number | null;
  token: string;
  /** Identificador de URL de la tienda (solo clientes), para volver a su página. */
  slug?: string;
}
