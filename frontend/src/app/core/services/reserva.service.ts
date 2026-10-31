import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse, NuevaReserva, Page, Reserva } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class ReservaService {
  private apiUrl = `${environment.apiUrl}/reservas`;

  constructor(private http: HttpClient) { }

  /** Cliente: sus reservas. Admin: todas. */
  listar(page = 0, size = 20): Observable<Page<Reserva>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<Page<Reserva>>>(this.apiUrl, { params }).pipe(map(r => r.data));
  }

  crear(reserva: NuevaReserva): Observable<Reserva> {
    return this.http.post<ApiResponse<Reserva>>(this.apiUrl, reserva).pipe(map(r => r.data));
  }

  cancelar(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`).pipe(map(() => undefined));
  }
}
