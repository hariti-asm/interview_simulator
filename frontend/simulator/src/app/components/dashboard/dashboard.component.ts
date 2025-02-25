import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { HttpClientModule } from '@angular/common/http';
import { InterviewSessionDTO } from '../../models/interview-sessiondto';
import { PerformanceData } from '../../models/performance-data';
import { AIInterviewService } from '../../services/ai-interview-service';
// Import Chart.js annotation plugin
import { Chart } from 'chart.js';
import annotationPlugin from 'chartjs-plugin-annotation';
import {HeaderComponent} from '../header/header.component';

// Register the annotation plugin
Chart.register(annotationPlugin);

// Define interfaces for performance data types
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

  // Recent interviews data
  recentInterviews: {
    position: string,
    company: string,
    date: string,
    score: number,
    status: string
  }[] = [];

  // Interview sessions from database
  interviewSessions: InterviewSessionDTO[] = [];

  // Skills data for radar chart
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

  // Topic performance data for bar chart
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

  // Performance improvement data
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

  // Improvement metrics
  skillImprovements: {
    skill: string,
    color: string,
    score: number,
    improvement: number
  }[] = [];

  // Stats cards data
  statsCards = [
    { icon: 'award', title: 'Success Rate', value: '0%', trend: '0%', color: 'blue' },
    { icon: 'brain', title: 'Total Interviews', value: '0', trend: '0', color: 'green' },
    { icon: 'book-open', title: 'Topics Covered', value: '0', trend: '0', color: 'purple' },
    { icon: 'bar-chart', title: 'Best Score', value: '0%', trend: '0%', color: 'yellow' }
  ];

  // Current user ID - replace with actual user authentication mechanism
  private userId: number = 1; // Example user ID

  constructor(private interviewService: AIInterviewService) {}

  ngOnInit(): void {
    this.loadInterviewData();
  }

  startNewInterview(): void {
    // Modal or navigation to interview setup page
    console.log('Starting new interview session');
    // Example of starting a new session using the service
    // this.interviewService.startNewSession(
    //   this.userId,
    //   'Software Engineer',
    //   'Full Stack',
    //   'Senior'
    // ).subscribe(session => {
    //   // Handle navigation to interview page
    //   console.log('New session created:', session);
    // });
  }

  // Load interview data from database
  loadInterviewData(): void {
    // Get interview sessions
    this.interviewService.getInterviewSessions(this.userId).subscribe(
      sessions => {
        this.interviewSessions = sessions;

        // Update total interviews stat
        this.statsCards[1].value = sessions.length.toString();
        this.statsCards[1].trend = '+' + (sessions.length > 0 ? 1 : 0);

        // Map sessions to recent interviews
        this.recentInterviews = sessions
          .slice(0, 5) // Get last 5 sessions
          .map((session: InterviewSessionDTO) => ({
            position: session.position,
            company: session.specialization, // Using specialization as company
            date: new Date(session.startTime).toISOString().split('T')[0],
            score: session.overallScore || 0,
            status: session.status
          }));

        // Load performance by skill data
        this.loadPerformanceBySkill();

        // Load overall performance summary
        this.loadPerformanceSummary();
      },
      error => {
        console.error('Error loading interview sessions:', error);
        // Load sample data as fallback
        this.loadSampleData();
      }
    );
  }

  // Load performance data by skill
  loadPerformanceBySkill(): void {
    this.interviewService.getPerformanceBySkill(this.userId).subscribe(
      skillData => {
        // Process skill data for radar chart
        this.processSkillData(skillData);

        // Process skill data for improvement metrics
        this.processSkillImprovements(skillData);

        // Process performance improvement chart
        this.processPerformanceImprovementData(skillData);
      },
      error => {
        console.error('Error loading skill performance data:', error);
      }
    );
  }

  // Load overall performance summary
  loadPerformanceSummary(): void {
    this.interviewService.getOverallPerformanceData(this.userId).subscribe(
      summary => {
        // Update success rate
        if (summary.successRate) {
          this.statsCards[0].value = summary.successRate.toFixed(1) + '%';
          this.statsCards[0].trend = '+' + (summary.successRateChange || 0).toFixed(1) + '%';
        }

        // Update topics covered
        if (summary.topicsCovered) {
          this.statsCards[2].value = summary.topicsCovered.toString();
          this.statsCards[2].trend = '+' + (summary.topicsAddedRecently || 0);
        }

        // Update best score
        if (summary.bestScore) {
          this.statsCards[3].value = summary.bestScore.toFixed(1) + '%';
          this.statsCards[3].trend = '+' + (summary.bestScoreImprovement || 0).toFixed(1) + '%';
        }

        // Update topic performance data
        if (summary.topicPerformance) {
          this.topicPerformanceData.labels = summary.topicPerformance.map((topic: TopicPerformanceItem) => topic.name);
          this.topicPerformanceData.datasets[0].data = summary.topicPerformance.map((topic: TopicPerformanceItem) => topic.score);
        }

        // Update performance trend
        if (summary.performanceTrend) {
          this.performanceData = summary.performanceTrend.map((item: PerformanceTrendItem) => ({
            month: item.month,
            score: item.score
          }));
        }
      },
      error => {
        console.error('Error loading performance summary:', error);
      }
    );
  }

  // Process skill data for radar chart
  private processSkillData(skillData: PerformanceData[]): void {
    // Get latest score for each skill
    const skills = skillData.map(skill => skill.skillName);
    const latestScores = skillData.map(skill => {
      const sortedScores = [...skill.scores].sort((a, b) =>
        new Date(b.date).getTime() - new Date(a.date).getTime()
      );
      return sortedScores.length > 0 ? sortedScores[0].score : 0;
    });

    // Update radar chart data
    this.skillsData.labels = skills;
    this.skillsData.datasets[0].data = latestScores;
  }

  // Process skill improvements
  private processSkillImprovements(skillData: PerformanceData[]): void {
    // Color map for skills
    const colorMap = {
      'Problem Solving': '#8B5CF6',
      'System Design': '#EC4899',
      'Communication': '#10B981',
      'Technical Knowledge': '#3B82F6',
      'Code Quality': '#F59E0B',
      // Default colors for other skills
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

  // Process performance improvement data for line chart
  private processPerformanceImprovementData(skillData: PerformanceData[]): void {
    // Define interfaces for better type safety
    interface SessionScore {
      sessionId: number;
      date: string;
      score: number;
    }

    interface SessionMapEntry {
      date: Date;
      label: string;
    }

    // Group sessions by date
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

    // Sort sessions by date
    const sortedSessions = Array.from(sessionsMap.entries())
      .sort((a, b) => a[1].date.getTime() - b[1].date.getTime())
      .map(entry => ({id: entry[0], label: entry[1].label}));

    // Create datasets for each skill
    const datasets = skillData.map(skill => {
      // Color map
      const colorMap: {[key: string]: string} = {
        'Problem Solving': '#8B5CF6',
        'System Design': '#EC4899',
        'Communication': '#10B981',
        'Technical Knowledge': '#3B82F6',
        'Code Quality': '#F59E0B'
      };

      const color = colorMap[skill.skillName] || '#6B7280';

      // Map scores to ordered sessions
      const data = sortedSessions.map(session => {
        const score = skill.scores.find(s => s.sessionId.toString() === session.id);
        return score ? score.score : null;
      });

      return {
        data: data,
        label: skill.skillName,
        borderColor: color,
        backgroundColor: color + '1A', // 10% opacity
        tension: 0.3,
        fill: false
      };
    });

    // Update chart data
    this.performanceImprovementData = {
      labels: sortedSessions.map(session => session.label),
      datasets: datasets
    };
  }

  // Filter interviews
  filterInterviews(): void {
    console.log('Filtering interviews');
  }

  // Export interviews
  exportInterviews(): void {
    console.log('Exporting interviews');
  }

  // Load sample data if database connection fails
  private loadSampleData(): void {
    // Performance trend data
    this.performanceData = [
      { month: 'Jan', score: 75 },
      { month: 'Feb', score: 82 },
      { month: 'Mar', score: 78 },
      { month: 'Apr', score: 85 },
      { month: 'May', score: 92 }
    ];

    // Recent interviews data
    this.recentInterviews = [
      { position: 'Senior Developer', company: 'Tech Corp', date: '2025-02-20', score: 85, status: 'Completed' },
      { position: 'Tech Lead', company: 'Innovation Labs', date: '2025-02-15', score: 92, status: 'Completed' }
    ];

    // Update stats cards
    this.statsCards = [
      { icon: 'award', title: 'Success Rate', value: '85%', trend: '+5%', color: 'blue' },
      { icon: 'brain', title: 'Total Interviews', value: '24', trend: '+2', color: 'green' },
      { icon: 'book-open', title: 'Topics Covered', value: '12', trend: '+3', color: 'purple' },
      { icon: 'bar-chart', title: 'Best Score', value: '92%', trend: '+8%', color: 'yellow' }
    ];

    // Update skills data
    this.skillsData.datasets[0].data = [85, 90, 88, 82, 75];

    // Update topic performance data
    this.topicPerformanceData.datasets[0].data = [88, 92, 75, 85, 78];

    // Improvement metrics
    this.skillImprovements = [
      { skill: 'Problem Solving', color: '#8B5CF6', score: 90, improvement: 5 },
      { skill: 'System Design', color: '#EC4899', score: 85, improvement: 7 },
      { skill: 'Communication', color: '#10B981', score: 92, improvement: 4 },
      { skill: 'Technical Knowledge', color: '#3B82F6', score: 88, improvement: 6 },
      { skill: 'Code Quality', color: '#F59E0B', score: 86, improvement: 6 }
    ];

    // Performance improvement data
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
