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

interface PositionCount {
  position: string;
  count: number;
}

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatDialogModule, MatSnackBarModule, FormsModule, HeaderComponent
  ],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css'],
})
export class AdminComponent implements OnInit {
  users: UserDTO[] = [];
  selectedUser: UserDTO | null = null;
  positionCounts: PositionCount[] = [];
  totalInterviews: number = 0;
  maxCount: number = 0; // Added for chart scaling

  private adminService = inject(AdminService);
  private interviewService = inject(InterviewService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  constructor() {
  }

  ngOnInit(): void {
    this.getAllUsers();
    this.loadPositionCounts();
  }

  loadPositionCounts(): void {
    this.interviewService.getInterviewPositionCounts().subscribe({
      next: (data) => {
        // Sort the data in ascending order by count
        this.positionCounts = data.sort((a, b) => a.count - b.count);
        this.totalInterviews = this.positionCounts.reduce((sum, item) => sum + item.count, 0);

        // Find maximum count for chart scaling
        this.maxCount = Math.max(...this.positionCounts.map(item => item.count));
      },
      error: (error) => {
        console.error('Error fetching position counts:', error);
      }
    });
  }

  // Rest of your component code remains the same
  getAllUsers(): void {
    this.adminService.getAllUsers().subscribe(users => {
      this.users = users;
    });
  }

  openEditDialog(user: UserDTO): void {
    this.selectedUser = {...user};
  }

  saveUser(): void {
    if (this.selectedUser) {
      this.adminService.updateUser(this.selectedUser.id, this.selectedUser).subscribe(() => {
        this.getAllUsers();
        this.closeDialog();
        this.snackBar.open('User updated successfully!', 'Close', {duration: 3000});
      });
    }
  }

  closeDialog(): void {
    this.selectedUser = null;
  }

  confirmDelete(userId: number): void {
    if (confirm('Are you sure you want to delete this user?')) {
      this.adminService.deleteUser(userId).subscribe(() => {
        this.getAllUsers();
        this.snackBar.open('User deleted successfully!', 'Close', {duration: 3000});
      });
    }
  }

  formatXAxisLabel(label: string): string {
    return label;
  }

  formatYAxisTick(value: number): string {
    return value.toString();
  }
}
