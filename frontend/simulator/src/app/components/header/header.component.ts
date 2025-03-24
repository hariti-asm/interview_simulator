import { Component, OnInit, HostListener } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NgIf, NgClass } from '@angular/common';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    NgIf,
    NgClass,
    CommonModule
  ]
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

  // Close dropdown when clicking outside
  @HostListener('document:click', ['$event'])
  onClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const dropdown = document.querySelector('.profile-dropdown-container');
    if (dropdown && !dropdown.contains(target) && this.showProfileDropdown) {
      this.showProfileDropdown = false;
    }
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

  toggleProfileDropdown(event?: Event): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    this.showProfileDropdown = !this.showProfileDropdown;
  }

  // Modified to ensure navigation works properly
  navigateAndCloseDropdown(route: string): void {
    console.log('Navigating to:', route);
    // Remove any conditions that might prevent navigation
    this.showProfileDropdown = false;
    this.menuOpen = false;

    // Use setTimeout to ensure UI state is updated before navigation
    setTimeout(() => {
      this.router.navigateByUrl(route).then(success => {
        console.log('Navigation result:', success);
      }).catch(error => {
        console.error('Navigation error:', error);
      });
    }, 0);
  }

  onStartInterview(): void {
    this.menuOpen = false;
    // Remove any conditions that might prevent navigation
    setTimeout(() => {
      if (this.isLoggedIn) {
        this.router.navigate(['/dashboard']);
      } else {
        this.router.navigate(['/login'], { queryParams: { returnUrl: '/dashboard' } });
      }
    }, 0);
  }

  logout(event?: Event): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    this.showProfileDropdown = false;
    this.menuOpen = false;

    this.authService.logout().subscribe(
      () => {
        this.isLoggedIn = false;
        this.userProfile = null;
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 0);
      },
      (error) => {
        console.error('Logout error:', error);
        this.isLoggedIn = false;
        this.userProfile = null;
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 0);
      }
    );
  }
}
