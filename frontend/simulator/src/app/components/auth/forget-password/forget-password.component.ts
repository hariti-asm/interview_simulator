import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import {NgClass, NgIf} from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forget-password',
  templateUrl: './forget-password.component.html',
  imports: [
    ReactiveFormsModule,
    NgIf,
    RouterLink,
    NgClass
  ],
  standalone: true
})
export class ForgetPasswordComponent {
  forgotForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService
  ) {
    this.forgotForm = this.formBuilder.group({
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.pattern('^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$')
      ]]
    });
  }

  onSubmit(): void {
    if (this.forgotForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      this.authService.forgotPassword(this.forgotForm.value.email)
        .subscribe({
          next: () => {
            this.successMessage = 'Password reset link has been sent to your email address.';
            this.forgotForm.reset();
            this.isLoading = false;
          },
          error: (error) => {
            this.isLoading = false;
            if (error.status === 404) {
              this.errorMessage = 'No account found with this email address.';
            } else if (error.status === 429) {
              this.errorMessage = 'Too many requests. Please try again later.';
            } else {
              this.errorMessage = 'Failed to send reset link. Please try again later.';
            }
            console.error('Password reset error:', error);
          }
        });
    } else {
      this.validateAllFormFields();
    }
  }

  private validateAllFormFields(): void {
    Object.keys(this.forgotForm.controls).forEach(field => {
      const control = this.forgotForm.get(field);
      control?.markAsTouched();
    });
  }

  // Helper method to check specific validation errors
  getEmailErrorMessage(): string {
    const emailControl = this.forgotForm.get('email');
    if (emailControl?.errors) {
      if (emailControl.errors['required']) {
        return 'Email is required';
      }
      if (emailControl.errors['email'] || emailControl.errors['pattern']) {
        return 'Please enter a valid email address';
      }
    }
    return '';
  }

  onEmailInput(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
