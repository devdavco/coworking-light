import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse, Disponibilidad, Espacio, Page, TipoEspacio } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class EspacioService {
  private apiUrl = `${environment.apiUrl}/espacios`;

  constructor(private http: HttpClient) { }

  listar(page = 0, size = 50, tipo?: TipoEspacio | ''): Observable<Page<Espacio>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (tipo) {
      params = params.set('tipo', tipo);
    }
    return this.http.get<ApiResponse<Page<Espacio>>>(this.apiUrl, { params }).pipe(map(r => r.data));
  }

  crear(espacio: Espacio): Observable<Espacio> {
    return this.http.post<ApiResponse<Espacio>>(this.apiUrl, espacio).pipe(map(r => r.data));
  }

  actualizar(id: number, espacio: Espacio): Observable<Espacio> {
    return this.http.put<ApiResponse<Espacio>>(`${this.apiUrl}/${id}`, espacio).pipe(map(r => r.data));
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`).pipe(map(() => undefined));
  }

  disponibilidad(id: number, fecha: string): Observable<Disponibilidad> {
    const params = new HttpParams().set('fecha', fecha);
    return this.http.get<ApiResponse<Disponibilidad>>(`${this.apiUrl}/${id}/disponibilidad`, { params })
      .pipe(map(r => r.data));
  }
}
