import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import {CommonModule, NgClass, NgIf} from '@angular/common';
import {AuthService} from '../../../services/auth.service';

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
  returnUrl: string = '/dashboard'; // Default return URL

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
    // Get return url from route parameters or default to '/'
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/dashboard';
    });

    // Check if there are stored credentials if remember me was previously checked
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
      const { email, password, rememberMe } = this.loginForm.value;

      this.authService.login(email, password).subscribe({
        next: (response) => {
          if (rememberMe) {
            localStorage.setItem('rememberedEmail', email);
          } else {
            localStorage.removeItem('rememberedEmail');
          }

          this.router.navigateByUrl(this.returnUrl);
        },
        error: (error) => {
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
