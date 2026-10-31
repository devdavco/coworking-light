import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const requiredRoles = route.data['roles'] as Role[] | undefined;

    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    const userRole = this.authService.getRole();
    if (userRole && requiredRoles.includes(userRole)) {
      return true;
    }

    // Autenticado pero sin permiso: a su propia página de inicio
    return this.router.parseUrl(this.authService.homeUrl());
  }
}
