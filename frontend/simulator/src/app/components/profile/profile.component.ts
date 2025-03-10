import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {HeaderComponent} from '../header/header.component';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HeaderComponent
  ]
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  userProfile: any = null;
  isLoading: boolean = true;
  isSaving: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    protected authService: AuthService,
    private formBuilder: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadUserProfile();
  }

  initForm(): void {
    this.profileForm = this.formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', Validators.pattern('^[0-9]{10}$')],
      bio: ['', Validators.maxLength(500)]
    });

    this.profileForm.get('email')?.disable();
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.authService.getUserProfile().subscribe(
      (profile) => {
        if (profile) {
          this.userProfile = profile;
          this.updateFormWithProfileData(profile);
        } else {
          if (!this.authService.isAuthenticated()) {
            this.router.navigate(['/login'], {
              queryParams: { returnUrl: '/profile' }
            });
          }
        }
        this.isLoading = false;
      },
      (error) => {
        console.error('Error loading profile data:', error);
        this.errorMessage = 'Failed to load profile data. Please try again later.';
        this.isLoading = false;
      }
    );
  }

  updateFormWithProfileData(profile: any): void {
    this.profileForm.patchValue({
      firstName: profile.firstName || '',

      email: profile.email || '',

    });
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.successMessage = '';
    this.errorMessage = '';

    const updatedProfile = {
      firstName: this.profileForm.get('firstName')?.value,
      email: this.profileForm.get('email')?.getRawValue()
    };

    console.log('Submitting profile update:', updatedProfile);

    this.authService.updateUserProfile(updatedProfile).subscribe({
      next: (response) => {
        console.log('Profile update success:', response);
        this.successMessage = 'Profile updated successfully';
        this.userProfile = response;
        this.isSaving = false;
      },
      error: (error) => {
        console.error('Error updating profile:', error);
        this.errorMessage = 'Failed to update profile. Please try again.';
        this.isSaving = false;
      }
    });
  }
  markAllAsTouched(): void {
    Object.keys(this.profileForm.controls).forEach(key => {
      this.profileForm.get(key)?.markAsTouched();
    });
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.profileForm.get(controlName);
    return !!(control && control.touched && control.hasError(errorName));
  }

  changePassword(): void {
    this.router.navigate(['/change-password']);
  }
}
