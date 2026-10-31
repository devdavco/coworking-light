import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

function fakeJwt(expSeconds: number): string {
  const payload = btoa(JSON.stringify({ sub: 'a@test.com', role: 'CLIENT', exp: expSeconds }));
  return `header.${payload}.firma`;
}

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('desenvuelve ApiResponse.data en el login y guarda la sesión', () => {
    const token = fakeJwt(Math.floor(Date.now() / 1000) + 3600);
    const user = { id: 1, email: 'a@test.com', firstName: 'A', lastName: 'B', role: 'ADMIN' as const };

    service.login('a@test.com', 'password123').subscribe(auth => {
      expect(auth.user.role).toBe('ADMIN');
    });

    http.expectOne(`${environment.apiUrl}/auth/login`).flush({
      success: true, message: 'ok', timestamp: '', data: { token, refreshToken: 'r', user }
    });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getRole()).toBe('ADMIN');
    expect(service.homeUrl()).toBe('/admin');
  });

  it('considera no autenticado un token expirado y limpia la sesión', () => {
    localStorage.setItem('token', fakeJwt(Math.floor(Date.now() / 1000) - 10));

    expect(service.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('considera no autenticado un token mal formado', () => {
    localStorage.setItem('token', 'basura');

    expect(service.isAuthenticated()).toBeFalse();
  });
});
