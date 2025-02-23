import { Component } from '@angular/core';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';
import { AuthService } from '../../services/auth.service';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  imports: [
    RouterLink,
    RouterLinkActive,
    NgIf
  ],
  standalone: true
})
export class HeaderComponent {
  menuOpen: boolean = false;

  constructor(private router: Router, private authService: AuthService) {}

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  onStartInterview(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    } else {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/dashboard' } });
    }
  }

  testLoginNavigation(): void {
    console.log('Attempting navigation to /login');
    this.router.navigate(['/login'])
      .then(() => console.log('Navigation completed'))
      .catch(err => console.error('Navigation failed:', err));
  }
}
