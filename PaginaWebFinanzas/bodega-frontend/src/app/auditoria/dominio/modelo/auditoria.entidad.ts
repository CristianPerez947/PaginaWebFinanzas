export interface RegistroAuditoria {
  id: number;
  fechaHora: string;
  tiendaId: number | null;
  usuario: string;
  rol: string;
  accion: string;
  entidad: string | null;
  entidadId: number | null;
  detalle: string | null;
}
