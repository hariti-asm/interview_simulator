import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {FormsModule} from '@angular/forms';
import {UserDTO} from '../../models/userdto';
import {AdminService} from '../../services/admin.service';
import {HeaderComponent} from '../header/header.component';
import {InterviewService} from '../../services/interview.service';
import {SkillDTO, SkillService} from '../../services/skill.service';
import {MatTooltip} from '@angular/material/tooltip';

interface PositionCount {
  position: string;
  count: number;
}

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule, MatIconModule,
    MatDialogModule, MatSnackBarModule, FormsModule, HeaderComponent, MatTooltip
  ],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css'],
})
export class AdminComponent implements OnInit {
  users: UserDTO[] = [];
  filteredUsers: UserDTO[] = [];
  selectedUser: UserDTO | null = null;
  positionCounts: PositionCount[] = [];
  totalInterviews: number = 0;
  totalSkills: number = 0;
  maxCount: number = 0;
  skills: SkillDTO[] = [];
  filteredSkills: SkillDTO[] = [];
  selectedSkill: SkillDTO | null = null;
  currentSkillPage: number = 1;

  userSearchQuery: string = '';
  userFilter: string = 'all';

  currentPage: number = 1;
  itemsPerPage: number = 10;
  skillSearchQuery: string = '';
  skillFilter: string = 'all';
  skillCurrentPage: number = 1;

  keywordsInput: string = '';
  positionsInput: string = '';

  totalPages: number = 1;

  Math = Math;
  activeSection: 'dashboard' | 'users' | 'skills' | 'interviews' = 'dashboard';

  private adminService = inject(AdminService);
  private skillService = inject(SkillService);
  private interviewService = inject(InterviewService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  constructor() {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  navigateTo(section: 'dashboard' | 'users' | 'skills' | 'interviews'): void {
    this.activeSection = section;

    switch (section) {
      case 'dashboard':
        this.loadDashboardData();
        break;
      case 'users':
        this.getAllUsers();
        break;
      case 'skills':
        this.getAllSkills();
        break;
      case 'interviews':
        this.loadPositionCounts();
        break;
    }
  }

  loadDashboardData(): void {
    this.getAllUsers();
    this.loadPositionCounts();
  }

  loadPositionCounts(): void {
    this.interviewService.getInterviewPositionCounts().subscribe({
      next: (data) => {
        this.positionCounts = data.sort((a, b) => a.count - b.count);
        this.totalInterviews = this.positionCounts.reduce((sum, item) => sum + item.count, 0);
        this.maxCount = Math.max(...this.positionCounts.map(item => item.count));
      },
      error: (error) => console.error('Error fetching position counts:', error)
    });
  }

  getAllUsers(): void {
    this.adminService.getAllUsers().subscribe(users => {
      this.users = users;
      this.currentPage=1;
      this.filterUsers();
    });
  }

  getAllSkills(): void {
    this.skillService.getAllSkills().subscribe(skills => {
      this.skills = skills;
      this.totalSkills = skills.length;
      this.filterSkills();
    });
  }


  filterUsers(): void {
    console.log('Filtering with:', this.userFilter);
    console.log('Available roles:', this.users.map(user => user.role));

    let filtered = [...this.users];
    this.filteredUsers = filtered;
    this.totalPages = Math.ceil(this.filteredUsers.length / this.itemsPerPage);
    this.currentPage = 1;
    if (this.userSearchQuery) {
      const query = this.userSearchQuery.toLowerCase();
      filtered = filtered.filter(user =>
        user.name.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query)
      );
    }

    if (this.userFilter === 'admin') {
      filtered = filtered.filter(user =>
        user.role && user.role.toLowerCase() === 'admin'
      );
      console.log('Admin filtered count:', filtered.length);
    }

    this.filteredUsers = filtered;
    this.currentPage = 1;
  }
  openAddUserDialog(): void {
    this.selectedUser = {  name: '', email: '', role: 'User' };
  }

  openEditDialog(user: UserDTO): void {
    this.selectedUser = { ...user };
  }
  saveUser(): void {
    if (this.selectedUser) {

      if (!this.selectedUser.password || this.selectedUser.password.trim().length === 0) {
        this.snackBar.open('Password is required!', 'Close', { duration: 3000 });
        return;
      }

      if (this.selectedUser.id === undefined) {
        this.adminService.createUser(this.selectedUser).subscribe(() => {
          this.getAllUsers();
          this.closeDialog();
          this.snackBar.open('Candidate created successfully!', 'Close', { duration: 3000 });
        });
      } else {
        this.adminService.updateUser(this.selectedUser.id, this.selectedUser).subscribe(() => {
          this.getAllUsers();
          this.closeDialog();
          this.snackBar.open('User updated successfully!', 'Close', { duration: 3000 });
        });
      }
    }
  }


  closeDialog(): void {
    this.selectedUser = null;
    this.selectedSkill = null;
    this.keywordsInput = '';
    this.positionsInput = '';
  }

  confirmDelete(userId: number): void {
    if (confirm('Are you sure you want to delete this user?')) {
      this.adminService.deleteUser(userId).subscribe(() => {
        this.getAllUsers();
        this.snackBar.open('User deleted successfully!', 'Close', { duration: 3000 });
      });
    }
  }


  filterSkills(): void {
    let filtered = [...this.skills];

    if (this.skillSearchQuery) {
      const query = this.skillSearchQuery.toLowerCase();
      filtered = filtered.filter(skill =>
        skill.name.toLowerCase().includes(query) ||
        skill.category.toLowerCase().includes(query)
      );
    }

    if (this.skillFilter !== 'all') {
      switch (this.skillFilter) {
        case 'technical':
          filtered = filtered.filter(skill => skill.category === 'Technical');
          break;
        case 'soft':
          filtered = filtered.filter(skill => skill.category === 'Soft');
          break;
        case 'active':
          filtered = filtered.filter(skill => skill.isActive === true);
          break;
        case 'inactive':
          filtered = filtered.filter(skill => skill.isActive === false);
          break;
      }
    }

    this.filteredSkills = filtered;
    this.skillCurrentPage = 1;
    this.totalPages = Math.ceil(this.filteredSkills.length / this.itemsPerPage);
  }

  openAddSkillDialog(): void {
    this.selectedSkill = {
      name: '',
      description: '',
      category: '',
      skillType: '',
      proficiencyLevels: [],
      keywords: [],
      relevantPositions: [],
      weight: 5,
      isActive: true
    };
    this.keywordsInput = '';
    this.positionsInput = '';
  }

  openEditSkillDialog(skill: SkillDTO): void {
    this.selectedSkill = { ...skill };

    this.keywordsInput = this.selectedSkill.keywords?.join(', ') || '';
    this.positionsInput = this.selectedSkill.relevantPositions?.join(', ') || '';
  }

  updateKeywords(): void {
    if (this.selectedSkill && this.keywordsInput) {
      const keywords = this.keywordsInput
        .split(',')
        .map(keyword => keyword.trim())
        .filter(keyword => keyword.length > 0);

      this.selectedSkill.keywords = keywords;
    }
  }

  updatePositions(): void {
    if (this.selectedSkill && this.positionsInput) {
      const positions = this.positionsInput
        .split(',')
        .map(position => position.trim())
        .filter(position => position.length > 0);

      this.selectedSkill.relevantPositions = positions;
    }
  }

  removeKeyword(index: number): void {
    if (this.selectedSkill?.keywords) {
      this.selectedSkill.keywords.splice(index, 1);
      this.keywordsInput = this.selectedSkill.keywords.join(', ');
    }
  }

  removePosition(index: number): void {
    if (this.selectedSkill?.relevantPositions) {
      this.selectedSkill.relevantPositions.splice(index, 1);
      this.positionsInput = this.selectedSkill.relevantPositions.join(', ');
    }
  }

  saveSkill(): void {
    if (this.selectedSkill) {
      this.updateKeywords();
      this.updatePositions();

      if (this.selectedSkill.id === 0 || this.selectedSkill.id === undefined) {
        this.skillService.createSkill(this.selectedSkill).subscribe({
          next: () => {
            this.getAllSkills();
            this.closeDialog();
            this.snackBar.open('Skill created successfully!', 'Close', {duration: 3000});
          },
          error: (error) => {
            console.error('Error creating skill:', error);
            this.snackBar.open('Failed to create skill. Please try again.', 'Close', {duration: 3000});
          }
        });
      } else {
        this.skillService.updateSkill(this.selectedSkill.id, this.selectedSkill).subscribe({
          next: () => {
            this.getAllSkills();
            this.closeDialog();
            this.snackBar.open('Skill updated successfully!', 'Close', {duration: 3000});
          },
          error: (error) => {
            console.error('Error updating skill:', error);
            this.snackBar.open('Failed to update skill. Please try again.', 'Close', {duration: 3000});
          }
        });
      }
    }
  }
  confirmDeleteSkill(skillId: number): void {
    if (confirm('Are you sure you want to delete this skill?')) {
      this.skillService.deleteSkill(skillId).subscribe({
        next: () => {
          this.getAllSkills();
          this.snackBar.open('Skill deleted successfully!', 'Close', {duration: 3000});
        },
        error: (error) => {
          console.error('Error deleting skill:', error);
          this.snackBar.open('Failed to delete skill. Please try again.', 'Close', {duration: 3000});
        }
      });
    }
  }

  changeSkillPage(newPage: number): void {
    if (newPage >= 1 && newPage <= this.totalPages) {
      this.currentSkillPage = newPage;
    }
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }
  get pagedUsers(): UserDTO[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = Math.min(startIndex + this.itemsPerPage, this.filteredUsers.length);
    return this.filteredUsers.slice(startIndex, endIndex);
  }
  get pagedSkills(): SkillDTO[] {
    const startIndex = (this.skillCurrentPage - 1) * this.itemsPerPage;
    const endIndex = Math.min(startIndex + this.itemsPerPage, this.filteredSkills.length);
    return this.filteredSkills.slice(startIndex, endIndex);
  }
  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }



}
