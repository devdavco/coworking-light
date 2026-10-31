/** Envoltorio común de todas las respuestas del backend (ApiResponse<T>). */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  pageNumber: number;
  pageSize: number;
}

export type Role = 'ADMIN' | 'CLIENT';

export interface Usuario {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: Usuario;
}

export type TipoEspacio = 'OFICINA' | 'SALA_REUNIONES' | 'COWORKING_ABIERTO';

export const TIPOS_ESPACIO: { value: TipoEspacio; label: string }[] = [
  { value: 'OFICINA', label: 'Oficina' },
  { value: 'SALA_REUNIONES', label: 'Sala de reuniones' },
  { value: 'COWORKING_ABIERTO', label: 'Coworking abierto' }
];

export interface Espacio {
  id?: number;
  nombre: string;
  descripcion?: string;
  tipo: TipoEspacio;
  capacidad: number;
  horarioApertura?: string;
  horarioCierre?: string;
}

export type EstadoReserva = 'CONFIRMADA' | 'CANCELADA';

export interface Reserva {
  id: number;
  espacioId: number;
  usuarioId: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: EstadoReserva;
  usuario: Usuario;
  espacio: Espacio;
}

export interface NuevaReserva {
  espacioId: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
}

export interface Franja {
  horaInicio: string;
  horaFin: string;
}

export interface Disponibilidad {
  espacioId: number;
  fecha: string;
  franjas: Franja[];
}

/** Extrae el mensaje de error que devuelve el backend, con un texto por defecto. */
export function mensajeError(err: any, porDefecto = 'Ha ocurrido un error'): string {
  const body = err?.error;
  if (body?.data && typeof body.data === 'object' && !Array.isArray(body.data)) {
    const detalles = Object.values(body.data).join('. ');
    if (detalles) {
      return detalles;
    }
  }
  return body?.message || porDefecto;
}
