import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import {NgClass, NgIf} from '@angular/common';
import { RouterLink } from '@angular/router';

enum UserRoles {
  CANDIDATE = 'candidate',
  ADMIN = 'admin'
}

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    NgIf,
    RouterLink,
    NgClass
  ],
  templateUrl: './register.component.html',
  standalone: true,
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  errorMessage: string = '';
  roles = UserRoles;

  constructor(private fb: FormBuilder) {
    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      role: [UserRoles.CANDIDATE],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      try {
        console.log('Form submitted:', this.registerForm.value);
      } catch (error) {
        this.errorMessage = 'Registration failed. Please try again.';
        console.error('Registration error:', error);
      }
    } else {
      this.errorMessage = 'Please fill in all required fields correctly.';
    }
  }
}
