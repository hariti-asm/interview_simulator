import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { InterviewService } from '../../services/interview.service'; // Update to use your actual interview service

interface InterviewSetupData {
  position: string;
  specialization: string;
  experienceLevel: string;
  questionCount: number;
}

@Component({
  selector: 'app-interview-popup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './interview-popup-component.component.html'
})
export class InterviewPopupComponent implements OnInit {
  interviewData: InterviewSetupData = {
    position: '',
    specialization: '',
    experienceLevel: '',
    questionCount: 10
  };

  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private router: Router,
    private interviewService: InterviewService, // Use the correct service
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.checkAuthentication();
  }

  private checkAuthentication(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
  }

  incrementQuestions(): void {
    if (this.interviewData.questionCount < 15) {
      this.interviewData.questionCount++;
    }
  }

  decrementQuestions(): void {
    if (this.interviewData.questionCount > 5) {
      this.interviewData.questionCount--;
    }
  }

  startNewInterview(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.interviewService.startInterview(
      this.interviewData.position,
      this.interviewData.specialization,
      this.interviewData.experienceLevel
    ).subscribe({
      next: (sessionData) => {
        this.isLoading = false;
        this.router.navigate(['/interview/session', sessionData.id]);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error starting interview:', error);
        this.errorMessage = 'Failed to start interview. Please try again.';
      }
    });
  }

  private validateForm(): boolean {
    if (!this.interviewData.position) {
      this.errorMessage = 'Please specify a position';
      return false;
    }
    if (!this.interviewData.specialization) {
      this.errorMessage = 'Please specify a specialization';
      return false;
    }
    if (!this.interviewData.experienceLevel) {
      this.errorMessage = 'Please specify an experience level';
      return false;
    }
    return true;
  }

  closePopup(): void {
    this.router.navigate(['/dashboard']);
  }
}
