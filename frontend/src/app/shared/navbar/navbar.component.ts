import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  template: `
    <nav class="navbar">
      <div class="nav-brand">Coworking · {{ title }}</div>
      <div class="nav-user" *ngIf="authService.currentUser$ | async as user">
        <span>{{ user.firstName }} {{ user.lastName }}</span>
        <button type="button" (click)="logout()" class="btn-logout">Cerrar sesión</button>
      </div>
    </nav>
  `
})
export class NavbarComponent {
  @Input() title = '';

  constructor(public authService: AuthService, private router: Router) { }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
