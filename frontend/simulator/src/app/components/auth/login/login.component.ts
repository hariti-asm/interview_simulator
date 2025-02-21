import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import {NgClass, NgIf} from '@angular/common';

@Component({
  selector: 'app-login',
  imports: [
    RouterLink,
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

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      rememberMe: [false]
    });
  }

  ngOnInit(): void {
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
      try {
        // Here you would typically call your authentication service
        console.log('Form submitted:', this.loginForm.value);

        // Handle remember me functionality
        if (this.loginForm.get('rememberMe')?.value) {
          localStorage.setItem('rememberedEmail', this.loginForm.get('email')?.value);
        } else {
          localStorage.removeItem('rememberedEmail');
        }


         this.router.navigate(['/dashboard']);
      } catch (error) {
        this.errorMessage = 'Login failed. Please check your credentials and try again.';
        console.error('Login error:', error);
      }
    } else {
      this.errorMessage = 'Please fill in all required fields correctly.';
    }
  }
}
