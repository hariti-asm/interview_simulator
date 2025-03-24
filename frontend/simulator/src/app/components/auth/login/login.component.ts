import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { CommonModule, NgClass, NgIf } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { Role } from '../../../models/role.enum';

@Component({
  selector: 'app-login',
  imports: [
    RouterLink,
    CommonModule,
    ReactiveFormsModule,
    NgIf,
    NgClass
  ],
  templateUrl: './login.component.html',
  standalone: true,
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage: string = '';
  returnUrl: string = '/dashboard';
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      rememberMe: [false]
    });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/dashboard';
    });

    const storedEmail = localStorage.getItem('rememberedEmail');
    if (storedEmail) {
      this.loginForm.patchValue({
        email: storedEmail,
        rememberMe: true
      });
    }
  }
  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const { email, password, rememberMe } = this.loginForm.value;

      this.authService.login(email, password, rememberMe).subscribe({
        next: (response) => {
          this.isLoading = false;

          if (rememberMe) {
            localStorage.setItem('rememberedEmail', email);
          } else {
            localStorage.removeItem('rememberedEmail');
          }

          console.log("Login response:", response);

          if (response.user && response.user.role) {
            console.log("User role:", response.user.role);
            console.log("Role type:", typeof response.user.role);

            // Compare as string to handle both enum and string cases
            const userRole = String(response.user.role);

            if (userRole === 'ADMIN') {
              console.log("User is an admin, redirecting to admin dashboard");
              this.router.navigateByUrl('/admin');
            } else {
              console.log("User is not an admin, redirecting to user dashboard");
              this.router.navigateByUrl(this.returnUrl);
            }
          } else {
            console.log("No role found in user object, using default redirect");
            this.router.navigateByUrl(this.returnUrl);
          }
        },
        error: (error) => {
          this.isLoading = false;
          if (error.status === 401) {
            this.errorMessage = 'Invalid email or password';
          } else {
            this.errorMessage = 'An error occurred during login';
          }
          console.error('Login error:', error);
        }
      });
    } else {
      this.errorMessage = 'Please fill in all required fields correctly.';
    }
  }
}
