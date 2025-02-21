import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import {NgClass, NgIf} from '@angular/common';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgClass,
    RouterLink
  ],
  standalone: true
})
export class ResetPasswordComponent implements OnInit {
  resetForm: FormGroup;
  token: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
    this.resetForm = this.formBuilder.group({
      newPassword: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)
      ]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.errorMessage = 'Invalid or missing reset token';
        // Optionally redirect to forgot password page
        // this.router.navigate(['/forgot-password']);
      }
    });
  }

  // Custom validator to check if passwords match
  private passwordMatchValidator(control: AbstractControl) {
    const password = control.get('newPassword');
    const confirmPassword = control.get('confirmPassword');

    if (password?.pristine || confirmPassword?.pristine) {
      return null;
    }

    return password && confirmPassword && password.value !== confirmPassword.value ?
      { 'passwordMismatch': true } : null;
  }

  // Helper method to get password error message
  getPasswordErrorMessage(): string {
    const passwordControl = this.resetForm.get('newPassword');
    if (passwordControl?.errors) {
      if (passwordControl.errors['required']) {
        return 'Password is required';
      }
      if (passwordControl.errors['minlength']) {
        return 'Password must be at least 8 characters long';
      }
      if (passwordControl.errors['pattern']) {
        return 'Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character';
      }
    }
    return '';
  }

  // Helper method to get confirm password error message
  getConfirmPasswordErrorMessage(): string {
    if (this.resetForm.errors?.['passwordMismatch']) {
      return 'Passwords do not match';
    }
    return '';
  }

  onSubmit(): void {
    if (this.resetForm.valid && this.token) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      this.authService.resetPassword(this.token, this.resetForm.value.newPassword)
        .subscribe({
          next: () => {
            this.successMessage = 'Password reset successful. You will be redirected to login...';
            this.isLoading = false;
            // Reset form
            this.resetForm.reset();
            // Redirect to login page after a short delay
            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 2000);
          },
          error: (error) => {
            this.isLoading = false;
            if (error.status === 400) {
              this.errorMessage = 'Invalid or expired reset token';
            } else if (error.status === 429) {
              this.errorMessage = 'Too many attempts. Please try again later.';
            } else {
              this.errorMessage = 'Failed to reset password. Please try again later.';
            }
            console.error('Password reset error:', error);
          }
        });
    } else {
      this.validateAllFormFields();
    }
  }

  private validateAllFormFields(): void {
    Object.keys(this.resetForm.controls).forEach(field => {
      const control = this.resetForm.get(field);
      control?.markAsTouched();
    });
  }

  // Reset messages when user starts typing
  onPasswordInput(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
