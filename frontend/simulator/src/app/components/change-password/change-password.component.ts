import { Component, OnInit } from "@angular/core"
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms"
import { HeaderComponent } from "../header/header.component"
import { AuthService } from '../../services/auth.service';
import { NgIf } from "@angular/common"

@Component({
  selector: "app-change-password",
  standalone: true,
  imports: [HeaderComponent, ReactiveFormsModule, NgIf],
  templateUrl: "./change-password.component.html",
  styleUrls: ["./change-password.component.css"],
})
export class ChangePasswordComponent implements OnInit {
  profileForm!: FormGroup
  isLoading = true
  successMessage: string | null = null
  errorMessage: string | null = null

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadUserProfile();
  }

  initializeForm(): void {
    this.profileForm = this.fb.group(
      {
        firstName: [""],
        email: [{ value: "", disabled: true }],
        currentPassword: ["", Validators.required],
        newPassword: ["", [Validators.required, Validators.minLength(6)]],
        confirmPassword: ["", Validators.required],
      },
      { validators: this.passwordMatchValidator }
    );
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.authService.getUserProfile().subscribe({
      next: (userProfile) => {
        console.log('User profile data received directly:', userProfile);
        this.isLoading = false;

        if (userProfile) {
          this.profileForm.patchValue({
            firstName: userProfile.firstName || '',
            email: userProfile.email || ''
          });
        } else {
          this.errorMessage = "Unable to retrieve user profile.";
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error("Error fetching user profile:", err);

        if (err.status === 401) {
          this.errorMessage = "Authentication error. Please log in again.";
        } else if (err.status === 404) {
          this.errorMessage = "User profile not found.";
        } else {
          this.errorMessage = "Failed to load profile: " + (err.error?.message || err.message || "Unknown error");
        }
      }
    });
  }
  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get("newPassword")?.value;
    const confirmPassword = form.get("confirmPassword")?.value;

    if (newPassword && confirmPassword && newPassword !== confirmPassword) {
      form.get("confirmPassword")?.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }

    return null;
  }

  onSubmit(): void {
    this.successMessage = null;
    this.errorMessage = null;

    if (this.profileForm.invalid) {
      this.errorMessage = "Please fill in all required fields correctly.";
      return;
    }

    const { currentPassword, newPassword, confirmPassword } = this.profileForm.value;
    if (newPassword !== confirmPassword) {
      this.errorMessage = "New password and confirmation do not match.";
      return;
    }

    this.authService.changePassword(currentPassword, newPassword).subscribe({
      next: (response) => {
        console.log('Password change successful:', response);
        this.successMessage = "Password changed successfully!";
        this.errorMessage = null;
        this.profileForm.patchValue({
          currentPassword: "",
          newPassword: "",
          confirmPassword: "",
        });
      },
      error: (err) => {
        console.error("Password change error:", err);

        if (err.status === 401 || (err.error && err.error.message === "Current password is incorrect")) {
          this.errorMessage = "Current password is incorrect. Please try again.";
        } else if (err.status === 400) {
          this.errorMessage = err.error?.message || "Invalid password format. Please check requirements.";
        } else {
          this.errorMessage = err.error?.message || "Failed to change password. Please try again later.";
        }
        this.successMessage = null;
      }
    });
  }


}
