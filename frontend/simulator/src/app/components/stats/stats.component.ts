import { Component } from '@angular/core';

@Component({
  selector: 'app-stats',
  imports: [],
  templateUrl: './stats.component.html',
  standalone: true,
  styleUrl: './stats.component.css'
})
export class StatsComponent {
  stats = [
    { value: '100K', label: 'User Active' },
    { value: '40%', label: 'Reduce Hiring Time' },
    { value: '30%', label: 'Interview accuracy' },
    { value: '20%', label: 'Employee rates' }
  ];

}
