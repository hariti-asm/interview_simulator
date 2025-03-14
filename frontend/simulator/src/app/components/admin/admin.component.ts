import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { UserDTO } from '../../models/userdto';
import { AdminService } from '../../services/admin.service';
import { HeaderComponent } from '../header/header.component';
import { InterviewService } from '../../services/interview.service';
import {SkillDTO, SkillService} from '../../services/skill.service';
import {MatTooltip} from '@angular/material/tooltip';

interface Skill {
  id: number;
  name: string;
  category: string;
  difficulty: string;
  isActive: boolean;
}

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
  maxCount: number = 0;
  skills: SkillDTO[] = [];
  filteredSkills: SkillDTO[] = [];
  selectedSkill: Skill | null = null;
  currentSkillPage: number = 1;

  userSearchQuery: string = '';
  userFilter: string = 'all';

  currentPage: number = 1;
  itemsPerPage: number = 10;
  skillSearchQuery: string = '';
  skillFilter: string = 'all';
  skillCurrentPage: number = 1;

  totalPages: number = 1; // Add this line to store the total pages

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
      this.filterUsers();
    });
  }

  filterUsers(): void {
    let filtered = [...this.users];

    if (this.userSearchQuery) {
      const query = this.userSearchQuery.toLowerCase();
      filtered = filtered.filter(user =>
        user.name.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query)
      );
    }

    if (this.userFilter === 'admin') {
      filtered = filtered.filter(user => user.role === 'Admin');
    }

    this.filteredUsers = filtered;
    this.currentPage = 1;
  }

  openAddUserDialog(): void {
    this.selectedUser = { id: 0, name: '', email: '', role: 'User' };
  }

  openEditDialog(user: UserDTO): void {
    this.selectedUser = { ...user };
  }

  saveUser(): void {
    if (this.selectedUser) {
      if (this.selectedUser.id === 0) {
        this.adminService.createUser(this.selectedUser).subscribe(() => {
          this.getAllUsers();
          this.closeDialog();
          this.snackBar.open('User created successfully!', 'Close', { duration: 3000 });
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
  }

  confirmDelete(userId: number): void {
    if (confirm('Are you sure you want to delete this user?')) {
      this.adminService.deleteUser(userId).subscribe(() => {
        this.getAllUsers();
        this.snackBar.open('User deleted successfully!', 'Close', { duration: 3000 });
      });
    }
  }

  getAllSkills(): void {
    this.skillService.getAllSkills().subscribe(skills => {
      this.skills = skills;
      this.filterSkills();
    });
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

    this.filteredSkills = filtered;
    this.skillCurrentPage = 1;
    this.totalPages = Math.ceil(this.filteredSkills.length / this.itemsPerPage); // Calculate total pages
  }

  openAddSkillDialog(): void {
    this.selectedSkill = { id: 0, name: '', category: '', difficulty: 'Beginner', isActive: true };
  }

  openEditSkillDialog(skill: Skill): void {
    this.selectedSkill = { ...skill };
  }

  saveSkill(): void {
    if (this.selectedSkill) {
      if (this.selectedSkill.id === 0) {
        this.skillService.createSkill(this.selectedSkill).subscribe(() => {
          this.getAllSkills();
          this.closeDialog();
          this.snackBar.open('Skill created successfully!', 'Close', { duration: 3000 });
        });
      } else {
        this.skillService.updateSkill(this.selectedSkill.id, this.selectedSkill).subscribe(() => {
          this.getAllSkills();
          this.closeDialog();
          this.snackBar.open('Skill updated successfully!', 'Close', { duration: 3000 });
        });
      }
    }
  }

  changeSkillPage(newPage: number): void {
    if (newPage >= 1 && newPage <= this.totalPages) {
      this.currentSkillPage = newPage;
      console.log(`Changed to page ${this.currentSkillPage}`);
    }
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  confirmDeleteSkill(skillId: number): void {
    if (confirm('Are you sure you want to delete this skill?')) {
      this.skillService.deleteSkill(skillId).subscribe(() => {
        this.getAllSkills();
        this.snackBar.open('Skill deleted successfully!', 'Close', { duration: 3000 });
      });
    }
  }
  changePage(page: number): void {
    if (page >= 1 && (page - 1) * this.itemsPerPage < this.filteredUsers.length) {
      this.currentPage = page;
    }
  }


  toggleSkillStatus(skill: Skill): void {
    const updatedSkill = { ...skill, isActive: !skill.isActive };
    this.skillService.updateSkill(skill.id, updatedSkill).subscribe(() => {
      this.getAllSkills();
      this.snackBar.open(`Skill ${updatedSkill.isActive ? 'activated' : 'deactivated'} successfully!`, 'Close', { duration: 3000 });
    });
  }
}
