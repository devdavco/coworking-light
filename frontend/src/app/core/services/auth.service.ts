import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse, AuthResponse, Role, Usuario } from '../models/api.models';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<Usuario | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  register(email: string, password: string, firstName: string, lastName: string): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, {
      email, password, firstName, lastName
    }).pipe(
      map(res => res.data),
      tap(auth => this.handleAuthResponse(auth))
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, {
      email, password
    }).pipe(
      map(res => res.data),
      tap(auth => this.handleAuthResponse(auth))
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSubject.next(null);
  }

  /** Hay token y no ha expirado. */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token || this.isTokenExpired(token)) {
      if (token) {
        this.logout();
      }
      return false;
    }
    return true;
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getUser(): Usuario | null {
    return this.currentUserSubject.value;
  }

  getRole(): Role | null {
    return this.getUser()?.role ?? null;
  }

  hasRole(role: Role): boolean {
    return this.getRole() === role;
  }

  /** Ruta de inicio según el rol del usuario. */
  homeUrl(): string {
    return this.hasRole('ADMIN') ? '/admin' : '/client';
  }

  private handleAuthResponse(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    this.currentUserSubject.next(response.user);
  }

  private loadUserFromStorage(): void {
    const user = localStorage.getItem(USER_KEY);
    if (!user) {
      return;
    }
    try {
      this.currentUserSubject.next(JSON.parse(user));
    } catch {
      this.logout();
    }
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return typeof payload.exp !== 'number' || payload.exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }
}
