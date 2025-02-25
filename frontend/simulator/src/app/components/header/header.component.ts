import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NgIf } from '@angular/common';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  imports: [
    RouterLink,
    RouterLinkActive,
    NgIf,
    CommonModule
  ],
  standalone: true
})
export class HeaderComponent implements OnInit {
  menuOpen: boolean = false;
  isLoggedIn: boolean = false;
  userProfile: any = null;
  showProfileDropdown: boolean = false;

  constructor(private router: Router, private authService: AuthService) {}

  ngOnInit(): void {
    this.checkAuthStatus();

    this.authService.authStateChanged.subscribe(() => {
      this.checkAuthStatus();
    });
  }

  checkAuthStatus(): void {
    this.isLoggedIn = this.authService.isAuthenticated();
    if (this.isLoggedIn) {
      this.getUserProfile();
    }
  }

  getUserProfile(): void {
    this.authService.getUserProfile().subscribe(
      profile => {
        this.userProfile = profile;
      },
      error => {
        console.error('Error fetching user profile:', error);
      }
    );
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  toggleProfileDropdown(): void {
    this.showProfileDropdown = !this.showProfileDropdown;
  }

  onStartInterview(): void {
    if (this.isLoggedIn) {
      this.router.navigate(['/dashboard']);
    } else {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/dashboard' } });
    }
  }

  logout(): void {
    this.authService.logout().subscribe(
      () => {
        this.isLoggedIn = false;
        this.userProfile = null;
        this.router.navigate(['/']);
        this.showProfileDropdown = false;
      },
      (error) => {
        console.error('Logout error:', error);
        this.isLoggedIn = false;
        this.userProfile = null;
        this.router.navigate(['/']);
        this.showProfileDropdown = false;
      }
    );
  }
}
