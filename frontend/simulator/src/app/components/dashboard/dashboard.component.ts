import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NgChartsModule} from 'ng2-charts';
import {Chart, ChartConfiguration, ChartData} from 'chart.js';
import {HttpClientModule} from '@angular/common/http';
import {InterviewSessionDTO} from '../../models/interview-sessiondto';
import {PerformanceData} from '../../models/performance-data';
import {AuthService} from '../../services/auth.service';
import {Router} from '@angular/router';
import annotationPlugin from 'chartjs-plugin-annotation';
import {HeaderComponent} from '../header/header.component';
import {InterviewService} from '../../services/interview.service';

Chart.register(annotationPlugin);

interface PerformanceTrendItem {
  month: string;
  score: number;
}

interface TopicPerformanceItem {
  name: string;
  score: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgChartsModule, HttpClientModule, HeaderComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  performanceData: {month: string, score: number}[] = [];

  recentInterviews: {
    id: number,
    position: string,
    company: string,
    date: string,
    score: number,
    status: string
  }[] = [];

  interviewSessions: InterviewSessionDTO[] = [];

  skillsData: ChartData<'radar'> = {
    labels: ['Technical', 'Communication', 'Problem Solving', 'System Design', 'Leadership'],
    datasets: [
      {
        data: [0, 0, 0, 0, 0],
        label: 'Skills',
        backgroundColor: 'rgba(129, 140, 248, 0.6)',
        borderColor: '#4F46E5',
        pointBackgroundColor: '#4F46E5',
      }
    ]
  };

  topicPerformanceData: ChartData<'bar'> = {
    labels: ['Algorithms', 'Database', 'System Design', 'API Design', 'Security'],
    datasets: [
      {
        data: [0, 0, 0, 0, 0],
        label: 'Score',
        backgroundColor: '#818CF8',
        borderRadius: 4
      }
    ]
  };

  performanceImprovementData: ChartData<'line'> = {
    labels: [],
    datasets: []
  };

  radarChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      r: {
        min: 0,
        max: 100,
        ticks: {
          stepSize: 20
        }
      }
    }
  };

  barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        min: 0,
        max: 100
      }
    }
  };

  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        min: 0,
        max: 100,
        title: {
          display: true,
          text: 'Performance Score'
        }
      }
    },
    plugins: {
      annotation: {
        annotations: {
          targetLine: {
            type: 'line',
            scaleID: 'y',
            value: 75,
            borderColor: 'red',
            borderWidth: 1,
            borderDash: [3, 3],
            label: {
              display: true,
              content: 'Target Score',
              position: 'end',
              backgroundColor: 'rgba(255, 99, 132, 0.8)'
            }
          }
        }
      }
    }
  };

  skillImprovements: {
    skill: string,
    color: string,
    score: number,
    improvement: number
  }[] = [];

  statsCards = [
    { icon: 'award', title: 'Success Rate', value: '0%', trend: '0%', color: 'blue' },
    { icon: 'brain', title: 'Total Interviews', value: '0', trend: '0', color: 'green' },
    { icon: 'book-open', title: 'Topics Covered', value: '0', trend: '0', color: 'purple' },
    { icon: 'bar-chart', title: 'Best Score', value: '0%', trend: '0%', color: 'yellow' }
  ];

  private userId: number | null = null;
  private userEmail: string | null = null;
  isLoading: boolean = true;
  hasError: boolean = false;
  errorMessage: string = '';

  constructor(
    private interviewService: InterviewService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkAuthentication();
  }

  private checkAuthentication(): void {
    console.log('Auth check - isAuthenticated:', this.authService.isAuthenticated());
    console.log('Auth check - token exists:', !!this.authService.getToken());

    if (!this.authService.isAuthenticated()) {
      console.log('Not authenticated according to auth service, redirecting to login');
      this.router.navigate(['/login']);
      return;
    }

    this.authService.getUserProfile().subscribe({
      next: (profile) => {
        if (profile) {
          this.userId = profile.id;
          console.log('Profile loaded successfully:', profile);
          this.loadInterviewData();
        } else {
          console.log('Profile is null, redirecting to login');
          this.router.navigate(['/login']);
        }
      },
      error: (error) => {
        console.error('Error loading user profile:', error);

        if (this.authService.isTokenExpired()) {
          console.log('Token is expired, redirecting to login');
          this.router.navigate(['/login']);
        } else {
          console.log('Profile error but token valid, continuing with default userId');
          this.loadSampleData();
        }
      }
    });
  }

  startNewInterview(): void {
    this.authService.getUserProfile().subscribe(
      (profile) => {
        if (profile) {
          this.userId = profile.id;
          console.log('Navigating to interview setup with user ID:', this.userId);
          this.router.navigate(['/interview/setup']);
        } else {
          console.log('No profile found, redirecting to login');
          this.router.navigate(['/login']);
        }
      },
      (error) => {
        console.error('Error loading profile:', error);
        this.router.navigate(['/login']);
      }
    );
  }

  loadInterviewData(): void {
    if (!this.userId) {
      this.setError('User not authenticated. Please log in.');
      return;
    }

    this.isLoading = true;

    this.interviewService.getUserInterviews(this.userId).subscribe(
      sessions => {
        this.interviewSessions = sessions;
        this.isLoading = false;

        if (sessions.length === 0) {
          console.log('No interview sessions found for user');
          this.loadSampleData();
          return;
        }

        this.statsCards[1].value = sessions.length.toString();
        this.statsCards[1].trend = '+' + (sessions.length > 0 ? 1 : 0);

        this.recentInterviews = sessions
          .slice(0, 5)
          .map((session: InterviewSessionDTO) => ({
            id:session.id,
            position: session.position,
            company: session.specialization,
            date: new Date(session.startTime).toISOString().split('T')[0],
            score: session.score || 0,
            status: session.status
          }));
        console.log("interviews of", this.recentInterviews);

      },
      error => {
        console.error('Error loading interview sessions:', error);
        this.isLoading = false;
        this.setError('Could not load interview data. Please try again later.');
        this.loadSampleData();
      }
    );
  }

  viewInterviewDetails(interviewId: number) {
    this.router.navigate(['/interviews', interviewId]);
  }

  deleteInterview(sessionId: number) {
    if (confirm("Are you sure you want to delete this interview?")) {
      this.interviewService.deleteInterview(sessionId).subscribe({
        next: (response) => {
          console.log(`Interview session ${sessionId} deleted successfully.`);
        },
        error: (err) => {
          console.error('Error deleting interview session:', err);
          console.error('Status:', err.status);
          console.error('Message:', err.message);
          console.error('Error details:', err.error);
        }
      });
    }
  }


  updatePerformanceSummary(summary: any): void {
    if (summary.successRate !== undefined) {
      this.statsCards[0].value = summary.successRate.toFixed(1) + '%';
      this.statsCards[0].trend = '+' + (summary.successRateChange || 0).toFixed(1) + '%';
    }

    if (summary.topicsCovered !== undefined) {
      this.statsCards[2].value = summary.topicsCovered.toString();
      this.statsCards[2].trend = '+' + (summary.topicsAddedRecently || 0);
    }

    if (summary.bestScore !== undefined) {
      this.statsCards[3].value = summary.bestScore.toFixed(1) + '%';
      this.statsCards[3].trend = '+' + (summary.bestScoreImprovement || 0).toFixed(1) + '%';
    }
  }

  loadPerformanceSummary(): void {
    if (!this.userId) return;

    this.interviewService.getOverallPerformanceData(this.userId).subscribe(
      summary => {
        this.updatePerformanceSummary(summary);

        if (summary.topicPerformance) {
          this.topicPerformanceData.labels = summary.topicPerformance.map((topic: TopicPerformanceItem) => topic.name);
          this.topicPerformanceData.datasets[0].data = summary.topicPerformance.map((topic: TopicPerformanceItem) => topic.score);
        }

        if (summary.performanceTrend) {
          this.performanceData = summary.performanceTrend.map((item: PerformanceTrendItem) => ({
            month: item.month,
            score: item.score
          }));
        }
      },
      error => {
        console.error('Error loading performance summary:', error);
        this.setError('Could not load performance summary data.');
      }
    );
  }

  private processSkillData(skillData: PerformanceData[]): void {
    const skills = skillData.map(skill => skill.skillName);
    const latestScores = skillData.map(skill => {
      const sortedScores = [...skill.scores].sort((a, b) =>
        new Date(b.date).getTime() - new Date(a.date).getTime()
      );
      return sortedScores.length > 0 ? sortedScores[0].score : 0;
    });

    this.skillsData.labels = skills;
    this.skillsData.datasets[0].data = latestScores;
  }

  private processSkillImprovements(skillData: PerformanceData[]): void {
    const colorMap = {
      'Problem Solving': '#8B5CF6',
      'System Design': '#EC4899',
      'Communication': '#10B981',
      'Technical Knowledge': '#3B82F6',
      'Code Quality': '#F59E0B',
      'default': '#6B7280'
    };

    this.skillImprovements = skillData.map(skill => {
      const sortedScores = [...skill.scores].sort((a, b) =>
        new Date(b.date).getTime() - new Date(a.date).getTime()
      );

      const latestScore = sortedScores.length > 0 ? sortedScores[0].score : 0;
      const previousScore = sortedScores.length > 1 ? sortedScores[1].score : 0;
      const improvement = latestScore - previousScore;

      return {
        skill: skill.skillName,
        color: colorMap[skill.skillName as keyof typeof colorMap] || colorMap.default,
        score: latestScore,
        improvement: improvement
      };
    });
  }

  private processPerformanceImprovementData(skillData: PerformanceData[]): void {
    interface SessionScore {
      sessionId: number;
      date: string;
      score: number;
    }

    interface SessionMapEntry {
      date: Date;
      label: string;
    }

    const sessionsMap = new Map<string, SessionMapEntry>();

    skillData.forEach(skill => {
      skill.scores.forEach((score) => {
        const date = score.date instanceof Date ? score.date : new Date(score.date);
        const dateStr = date.toISOString().split('T')[0];
        const sessionId = score.sessionId;

        if (!sessionsMap.has(sessionId.toString())) {
          sessionsMap.set(sessionId.toString(), {
            date,
            label: `Interview ${sessionsMap.size + 1}`
          });
        }
      });
    });

    const sortedSessions = Array.from(sessionsMap.entries())
      .sort((a, b) => a[1].date.getTime() - b[1].date.getTime())
      .map(entry => ({id: entry[0], label: entry[1].label}));

    const datasets = skillData.map(skill => {
      const colorMap: {[key: string]: string} = {
        'Problem Solving': '#8B5CF6',
        'System Design': '#EC4899',
        'Communication': '#10B981',
        'Technical Knowledge': '#3B82F6',
        'Code Quality': '#F59E0B'
      };

      const color = colorMap[skill.skillName] || '#6B7280';

      const data = sortedSessions.map(session => {
        const score = skill.scores.find(s => s.sessionId.toString() === session.id);
        return score ? score.score : null;
      });

      return {
        data: data,
        label: skill.skillName,
        borderColor: color,
        backgroundColor: color + '1A',
        tension: 0.3,
        fill: false
      };
    });

    this.performanceImprovementData = {
      labels: sortedSessions.map(session => session.label),
      datasets: datasets
    };
  }

  filterInterviews(): void {
    console.log('Filtering interviews');
  }

  exportInterviews(): void {
    console.log('Exporting interviews');
  }

  private setError(message: string): void {
    this.hasError = true;
    this.errorMessage = message;
    console.error(message);
  }

  private loadSampleData(): void {
    this.performanceData = [
      { month: 'Jan', score: 75 },
      { month: 'Feb', score: 82 },
      { month: 'Mar', score: 78 },
      { month: 'Apr', score: 85 },
      { month: 'May', score: 92 }
    ];



    this.statsCards = [
      { icon: 'award', title: 'Success Rate', value: '85%', trend: '+5%', color: 'blue' },
      { icon: 'brain', title: 'Total Interviews', value: '24', trend: '+2', color: 'green' },
      { icon: 'book-open', title: 'Topics Covered', value: '12', trend: '+3', color: 'purple' },
      { icon: 'bar-chart', title: 'Best Score', value: '92%', trend: '+8%', color: 'yellow' }
    ];

    this.skillsData.datasets[0].data = [85, 90, 88, 82, 75];

    this.topicPerformanceData.datasets[0].data = [88, 92, 75, 85, 78];

    this.skillImprovements = [
      { skill: 'Problem Solving', color: '#8B5CF6', score: 90, improvement: 5 },
      { skill: 'System Design', color: '#EC4899', score: 85, improvement: 7 },
      { skill: 'Communication', color: '#10B981', score: 92, improvement: 4 },
      { skill: 'Technical Knowledge', color: '#3B82F6', score: 88, improvement: 6 },
      { skill: 'Code Quality', color: '#F59E0B', score: 86, improvement: 6 }
    ];

    this.performanceImprovementData = {
      labels: ['Interview 1', 'Interview 2', 'Interview 3', 'Interview 4', 'Latest'],
      datasets: [
        {
          data: [65, 72, 78, 85, 90],
          label: 'Problem Solving',
          borderColor: '#8B5CF6',
          backgroundColor: 'rgba(139, 92, 246, 0.1)',
          tension: 0.3,
          fill: false
        },
        {
          data: [55, 63, 70, 78, 85],
          label: 'System Design',
          borderColor: '#EC4899',
          backgroundColor: 'rgba(236, 72, 153, 0.1)',
          tension: 0.3,
          fill: false
        },
        {
          data: [70, 75, 82, 88, 92],
          label: 'Communication',
          borderColor: '#10B981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          tension: 0.3,
          fill: false
        },
        {
          data: [60, 68, 75, 82, 88],
          label: 'Technical Knowledge',
          borderColor: '#3B82F6',
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          tension: 0.3,
          fill: false
        },
        {
          data: [58, 65, 73, 80, 86],
          label: 'Code Quality',
          borderColor: '#F59E0B',
          backgroundColor: 'rgba(245, 158, 11, 0.1)',
          tension: 0.3,
          fill: false
        }
      ]
    };
  }
}
