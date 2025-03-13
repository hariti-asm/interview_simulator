import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { InterviewService } from '../../services/interview.service';
import { SkillDTO, SkillService } from '../../services/skill.service';

interface SelectedSkill {
  skillId: number;
  proficiencyLevel: number;
  isRequired: boolean;
  notes?: string;
}

interface InterviewSetupData {
  position: string;
  specialization: string;
  experienceLevel: string;
  questionCount: number;
  skills: SelectedSkill[];
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
    questionCount: 10,
    skills: []
  };

  availableSkills: SkillDTO[] = [];
  filteredSkills: SkillDTO[] = [];
  selectedSkillId: number | null = null;
  selectedSkillProficiency: number = 1;
  selectedSkillRequired: boolean = true;
  selectedSkillNotes: string = '';

  errorMessage: string = '';
  isLoading: boolean = false;
  currentUser: any = null;

  constructor(
    private router: Router,
    private interviewService: InterviewService,
    private authService: AuthService,
    private skillService: SkillService
  ) {}

  ngOnInit(): void {
    this.checkAuthentication();
    this.authService.userProfile.subscribe(profile => {
      this.currentUser = profile;
    });
    this.loadSkills();
  }

  private checkAuthentication(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
  }

  private loadSkills(): void {
    this.skillService.getAllSkills().subscribe({
      next: (skills) => {
        this.availableSkills = skills;
        this.filteredSkills = skills;
      },
      error: (error) => {
        console.error('Error loading skills:', error);
        this.errorMessage = 'Failed to load skills. Please refresh the page.';
      }
    });
  }

  filterSkills(event: any): void {
    const query = event.target.value.toLowerCase();
    this.filteredSkills = this.availableSkills.filter(skill =>
      skill.name.toLowerCase().includes(query) ||
      skill.category.toLowerCase().includes(query)
    );
  }

  addSkill(): void {
    if (!this.selectedSkillId) {
      return;
    }

    const skillExists = this.interviewData.skills.some(s => s.skillId === this.selectedSkillId);
    if (skillExists) {
      this.errorMessage = 'This skill is already added to the interview.';
      return;
    }

    this.interviewData.skills.push({
      skillId: this.selectedSkillId,
      proficiencyLevel: this.selectedSkillProficiency,
      isRequired: this.selectedSkillRequired,
      notes: this.selectedSkillNotes
    });

    this.resetSkillSelection();
  }

  private resetSkillSelection(): void {
    this.selectedSkillId = null;
    this.selectedSkillProficiency = 1;
    this.selectedSkillRequired = true;
    this.selectedSkillNotes = '';
  }

  removeSkill(skillId: number): void {
    this.interviewData.skills = this.interviewData.skills.filter(s => s.skillId !== skillId);
  }

  getSkillById(id: number): SkillDTO | undefined {
    return this.availableSkills.find(skill => skill.id === id);
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

    this.interviewService.startInterview(
      this.interviewData.position,
      this.interviewData.specialization,
      this.interviewData.experienceLevel,
      this.interviewData.skills
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
    if (this.interviewData.skills.length === 0) {
      this.errorMessage = 'Please add at least one skill for the interview';
      return false;
    }
    return true;
  }

  closePopup(): void {
    this.router.navigate(['/dashboard']);
  }
}
