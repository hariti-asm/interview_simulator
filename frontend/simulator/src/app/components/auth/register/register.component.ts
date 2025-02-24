import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { NgClass, NgIf } from '@angular/common';
import { RouterLink, RouterModule, Router } from '@angular/router';
import {Role} from '../../../models/role.enum';
import {AuthService} from '../../../services/auth.service';
import {RegisterRequest} from '../../../models/register-request';
;

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    NgIf,
    RouterLink,
    NgClass,
    RouterModule
  ],
  templateUrl: './register.component.html',
  standalone: true,
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;
  roles = Role;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      role: [Role.CANDIDATE],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });
  }

  ngOnInit(): void {}

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const registerData: RegisterRequest = {
        name: this.registerForm.get('name')?.value,
        email: this.registerForm.get('email')?.value,
        role: this.registerForm.get('role')?.value,
        password: this.registerForm.get('password')?.value,
        rememberMe: this.registerForm.get('rememberMe')?.value
      };

      this.authService.register(registerData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.router.navigate(['/login']);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Registration failed. Please try again.';
          console.error('Registration error:', error);
        }
      });
    } else {
      this.errorMessage = 'Please fill in all required fields correctly.';
    }
  }
}
