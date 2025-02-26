import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AIInterviewService } from '../../services/ai-interview-service';
import { AuthService } from '../../services/auth.service';

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

  private userId: number | null = null;

  constructor(
    private router: Router,
    private interviewService: AIInterviewService,
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

    this.authService.getUserProfile().subscribe(
      (profile) => {
        if (profile) {
          this.userId = profile.id;
        } else {
          // Handle missing profile
          this.router.navigate(['/login']);
        }
      },
      (error) => {
        console.error('Error loading user profile:', error);
        this.router.navigate(['/login']);
      }
    );
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

  startInterview(): void {
    if (!this.userId) {
      this.router.navigate(['/login']);
      return;
    }

    // Create a new interview session
    this.interviewService.startNewSession({
      userId: this.userId,
      position: this.interviewData.position,
      specialization: this.interviewData.specialization,
      experienceLevel: this.interviewData.experienceLevel,
      questionCount: this.interviewData.questionCount
    }).subscribe(
      (session) => {
        // Navigate to the interview room with the session ID
        this.router.navigate(['/interview/room', session.id]);
      },
      (error) => {
        console.error('Error creating interview session:', error);
        // Here you'd typically show an error message to the user
      }
    );
  }

  closePopup(): void {
    this.router.navigate(['/dashboard']);
  }
}
