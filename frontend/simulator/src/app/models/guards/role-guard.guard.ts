// role-guard.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take, catchError } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {

    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    const requiredRole = route.data['role'] as string;

    return this.authService.getUserProfile().pipe(
      take(1),
      map(profile => {

        const userRole = profile.role;

        if (userRole === requiredRole) {
          return true;
        } else {
          console.log(`Access denied: User role ${userRole} does not match required role ${requiredRole}`);
          this.router.navigate(['/dashboard']);
          return false;
        }
      }),
      catchError(error => {
        console.error('Error getting user profile:', error);
        this.router.navigate(['/dashboard']);
        return of(false);
      })
    );
  }
}
