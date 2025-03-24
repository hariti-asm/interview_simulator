import { Component, type OnInit } from "@angular/core"
import { CommonModule } from "@angular/common"
import { NgChartsModule } from "ng2-charts"
import { Chart, type ChartConfiguration, type ChartData } from "chart.js"
import { HttpClientModule } from "@angular/common/http"
import  { InterviewSessionDTO } from "../../models/interview-sessiondto"
import  { PerformanceData } from "../../models/performance-data"
import { AuthService } from "../../services/auth.service"
import  { Router } from "@angular/router"
import annotationPlugin from "chartjs-plugin-annotation"
import { HeaderComponent } from "../header/header.component"
import  { InterviewService } from "../../services/interview.service"

Chart.register(annotationPlugin)

@Component({
  selector: "app-dashboard",
  standalone: true,
  imports: [CommonModule, NgChartsModule, HttpClientModule, HeaderComponent],
  templateUrl: "./dashboard.component.html",
  styleUrl: "./dashboard.component.css",
})
export class DashboardComponent implements OnInit {
  Math = Math
  performanceData: { month: string; score: number }[] = []

  recentInterviews: {
    id: number
    position: string
    company: string
    date: string
    score: number
    status: string
  }[] = []

  interviewSessions: InterviewSessionDTO[] = []

  skillsData: ChartData<"radar"> = {
    labels: ["Technical", "Communication", "Problem Solving", "System Design", "Leadership"],
    datasets: [
      {
        data: [0, 0, 0, 0, 0],
        label: "Current Skills",
        backgroundColor: "rgba(99, 102, 241, 0.2)",
        borderColor: "rgb(99, 102, 241)",
        pointBackgroundColor: "rgb(99, 102, 241)",
        pointHoverBackgroundColor: "#fff",
        pointHoverBorderColor: "rgb(99, 102, 241)",
      },
      {
        data: [0, 0, 0, 0, 0],
        label: "Previous Assessment",
        backgroundColor: "rgba(209, 213, 219, 0.2)",
        borderColor: "rgb(209, 213, 219)",
        pointBackgroundColor: "rgb(209, 213, 219)",
        pointHoverBackgroundColor: "#fff",
        pointHoverBorderColor: "rgb(209, 213, 219)",
        borderDash: [5, 5],
      },
    ],
  }

  topicPerformanceData: ChartData<"bar"> = {
    labels: ["Algorithms", "Database", "System Design", "API Design", "Security"],
    datasets: [
      {
        data: [0, 0, 0, 0, 0],
        label: "Your Score",
        backgroundColor: [
          "rgba(99, 102, 241, 0.8)",
          "rgba(79, 70, 229, 0.8)",
          "rgba(124, 58, 237, 0.8)",
          "rgba(139, 92, 246, 0.8)",
          "rgba(167, 139, 250, 0.8)",
        ],
        borderRadius: 6,
        borderWidth: 1,
        borderColor: [
          "rgb(99, 102, 241)",
          "rgb(79, 70, 229)",
          "rgb(124, 58, 237)",
          "rgb(139, 92, 246)",
          "rgb(167, 139, 250)",
        ],
      },
      {
        data: [70, 75, 80, 72, 68],
        label: "Industry Average",
        backgroundColor: "rgba(209, 213, 219, 0.5)",
        borderRadius: 6,
        borderWidth: 1,
        borderColor: "rgb(209, 213, 219)",
      },
    ],
  }

  performanceImprovementData: ChartData<"line"> = {
    labels: [],
    datasets: [],
  }

  radarChartOptions: ChartConfiguration["options"] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      r: {
        min: 0,
        max: 100,
        ticks: {
          stepSize: 20,
          backdropColor: "transparent",
        },
        grid: {
          color: "rgba(229, 231, 235, 0.5)",
        },
        angleLines: {
          color: "rgba(229, 231, 235, 0.5)",
        },
        pointLabels: {
          font: {
            size: 12,
            weight: "bold",
          },
        },
      },
    },
    plugins: {
      legend: {
        position: "top",
        labels: {
          padding: 20,
          usePointStyle: true,
          pointStyleWidth: 10,
        },
      },
      tooltip: {
        backgroundColor: "rgba(249, 250, 251, 0.9)",
        titleColor: "#111827",
        bodyColor: "#374151",
        borderColor: "rgba(229, 231, 235, 1)",
        borderWidth: 1,
        padding: 12,
        boxPadding: 6,
        usePointStyle: true,
      },
    },
  }

  barChartOptions: ChartConfiguration["options"] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        min: 0,
        max: 100,
        grid: {
          color: "rgba(243, 244, 246, 1)",
        },
        ticks: {
          stepSize: 20,
        },
        title: {
          display: true,
          text: "Score",
          font: {
            weight: "bold",
          },
        },
      },
      x: {
        grid: {
          display: false,
        },
        ticks: {
          font: {
            weight: "bold",
          },
        },
      },
    },
    plugins: {
      legend: {
        position: "top",
        labels: {
          padding: 20,
          usePointStyle: true,
          pointStyleWidth: 10,
        },
      },
      tooltip: {
        backgroundColor: "rgba(249, 250, 251, 0.9)",
        titleColor: "#111827",
        bodyColor: "#374151",
        borderColor: "rgba(229, 231, 235, 1)",
        borderWidth: 1,
        padding: 12,
        boxPadding: 6,
      },
    },
  }

  lineChartOptions: ChartConfiguration["options"] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        min: 0,
        max: 100,
        grid: {
          color: "rgba(243, 244, 246, 1)",
        },
        ticks: {
          stepSize: 20,
        },
        title: {
          display: true,
          text: "Performance Score",
          font: {
            weight: "bold",
          },
        },
      },
      x: {
        grid: {
          display: false,
        },
        ticks: {
          font: {
            weight: "bold",
          },
        },
      },
    },
    plugins: {
      legend: {
        position: "top",
        labels: {
          padding: 20,
          usePointStyle: true,
          pointStyleWidth: 10,
        },
      },
      tooltip: {
        backgroundColor: "rgba(249, 250, 251, 0.9)",
        titleColor: "#111827",
        bodyColor: "#374151",
        borderColor: "rgba(229, 231, 235, 1)",
        borderWidth: 1,
        padding: 12,
        boxPadding: 6,
      },
      annotation: {
        annotations: {
          targetLine: {
            type: "line",
            scaleID: "y",
            value: 75,
            borderColor: "rgba(239, 68, 68, 0.8)",
            borderWidth: 2,
            borderDash: [5, 5],
            label: {
              display: true,
              content: "Target Score",
              position: "end",
              backgroundColor: "rgba(239, 68, 68, 0.8)",
              font: {
                weight: 500,
                size: 12,
              },
              padding: 6,
            },
          },
        },
      },
    },
  }

  skillImprovements: {
    skill: string
    color: string
    score: number
    improvement: number
  }[] = []

  statsCards = [
    { icon: "award", title: "Success Rate", value: "0%", trend: "0%", color: "blue" },
    { icon: "brain", title: "Total Interviews", value: "0", trend: "0", color: "green" },
    { icon: "book-open", title: "Topics Covered", value: "0", trend: "0", color: "purple" },
    { icon: "bar-chart", title: "Best Score", value: "0%", trend: "0%", color: "yellow" },
  ]

  private userId: number | null = null
  private userEmail: string | null = null
  isLoading = true
  hasError = false
  errorMessage = ""

  private refreshTimer: any

  constructor(
    private interviewService: InterviewService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.checkAuthentication()
    this.startAutoRefresh()
  }

  ngOnDestroy(): void {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
  }

  private checkAuthentication(): void {
    console.log("Auth check - isAuthenticated:", this.authService.isAuthenticated())
    console.log("Auth check - token exists:", !!this.authService.getToken())

    if (!this.authService.isAuthenticated()) {
      console.log("Not authenticated according to auth service, redirecting to login")
      this.router.navigate(["/login"])
      return
    }

    this.authService.getUserProfile().subscribe({
      next: (profile) => {
        if (profile) {
          this.userId = profile.id
          console.log("Profile loaded successfully:", profile)
          this.loadInterviewData()
        } else {
          console.log("Profile is null, redirecting to login")
          this.router.navigate(["/login"])
        }
      },
      error: (error) => {
        console.error("Error loading user profile:", error)

        if (this.authService.isTokenExpired()) {
          console.log("Token is expired, redirecting to login")
          this.router.navigate(["/login"])
        } else {
          console.log("Profile error but token valid, continuing with default userId")
          this.loadSampleData()
        }
      },
    })
  }
  startNewInterview(): void {
    this.authService.getUserProfile().subscribe({
      next: (profile) => {
        if (profile) {
          this.userId = profile.id
          console.log("Navigating to interview setup with user ID:", this.userId)
          this.router.navigate(["/interview/setup"], {
            queryParams: { returnUrl: '/dashboard' }
          })
        } else {
          console.log("No profile found, redirecting to login")
          this.router.navigate(["/login"])
        }
      },
      error: (error) => {
        console.error("Error loading profile:", error)
        this.router.navigate(["/login"])
      },
    })
  }

  loadInterviewData(): void {
    if (!this.userId) {
      this.setError("User not authenticated. Please log in.")
      return
    }

    this.isLoading = true

    this.interviewService.getUserInterviews(this.userId).subscribe({
      next: (sessions) => {
        this.interviewSessions = sessions
        this.isLoading = false

        if (sessions.length === 0) {
          console.log("No interview sessions found for user")
          return
        }

        const totalInterviews = sessions.length
        this.statsCards[1].value = totalInterviews.toString()
        this.statsCards[1].trend = "+" + (totalInterviews > 0 ? 1 : 0)

        const successfulInterviews = sessions.filter((session) => (session.score || 0) > 70).length
        const successRate = totalInterviews > 0 ? (successfulInterviews / totalInterviews) * 100 : 0
        this.statsCards[0].value = successRate.toFixed(1) + "%"

        const scores = sessions.map((session) => session.score || 0)
        const bestScore = scores.length > 0 ? Math.max(...scores) : 0
        this.statsCards[3].value = bestScore.toFixed(1) + "%"

        this.recentInterviews = sessions.slice(0, 5).map((session: InterviewSessionDTO) => ({
          id: session.id,
          position: session.position,
          company: session.specialization,
          date: new Date(session.startTime).toISOString().split("T")[0],
          score: session.score || 0,
          status: session.status,
        }))

        this.loadPerformanceSummary()
        this.loadSkillsData()
      },
      error: (error) => {
        console.error("Error loading interview sessions:", error)
        this.isLoading = false
        this.setError("Could not load interview data. Please try again later.")
      },
    })
  }

  loadSkillsData(): void {
    if (!this.userId) return

    this.interviewService.getUserSkillPerformance(this.userId).subscribe({
      next: (skillData: PerformanceData[]) => {
        if (skillData && skillData.length > 0) {
          console.log("Skills data loaded successfully:", skillData)
          this.processSkillData(skillData)
          this.processSkillImprovements(skillData)
          this.processPerformanceImprovementData(skillData)

          this.updateSkillsRadarChart()
        } else {
          console.log("No skills data available")
        }
      },
      error: (error) => {
        console.error("Error loading skills data:", error)
      },
    })
  }

  viewInterviewDetails(interviewId: number) {
    this.router.navigate(["/interviews", interviewId])
  }

  deleteInterview(sessionId: number) {
    if (confirm("Are you sure you want to delete this interview?")) {
      this.interviewService.deleteInterview(sessionId).subscribe({
        next: (response) => {
          console.log(`Interview session ${sessionId} deleted successfully.`)
          // Reload data after deletion
          this.loadInterviewData()
        },
        error: (err) => {
          console.error("Error deleting interview session:", err)
          console.error("Status:", err.status)
          console.error("Message:", err.message)
          console.error("Error details:", err.error)
        },
      })
    }
  }

  loadPerformanceSummary(): void {
    if (!this.userId) return

    this.interviewService.getOverallPerformanceData(this.userId).subscribe({
      next: (summary) => {
        console.log("Performance summary loaded:", summary)

        const scaleFactor = 100

        this.statsCards[0].value = `${(summary.successRate || 0).toFixed(1)}%`
        this.statsCards[0].trend = `+${(summary.successRateChange || 0).toFixed(1)}%`

        this.statsCards[1].value = `${summary.totalInterviews || 0}`
        this.statsCards[1].trend = "+0"

        this.statsCards[2].value = `${summary.topicsCovered || 0}`
        this.statsCards[2].trend = `+${summary.topicsAddedRecently || 0}`

        const scaledBestScore = summary.bestScore ? summary.bestScore / scaleFactor : 0
        this.statsCards[3].value = `${scaledBestScore.toFixed(1)}%`

        const scaledImprovement = summary.bestScoreImprovement ? summary.bestScoreImprovement / scaleFactor : 0
        this.statsCards[3].trend = `+${scaledImprovement.toFixed(1)}%`

        if (summary.performanceTrend && summary.performanceTrend.length > 0) {
          const scaledTrendData = summary.performanceTrend.map((item: { date: any; score: number }) => ({
            ...item,
            score: item.score / scaleFactor,
          }))
          this.updatePerformanceTrendChart(scaledTrendData)
        }

        if (summary.topicPerformance && summary.topicPerformance.length > 0) {
          const scaledTopicData = summary.topicPerformance.map((item: { topic: string; score: number }) => ({
            ...item,
            score: item.score / scaleFactor,
          }))
          this.updateTopicPerformanceChart(scaledTopicData)
        }

        this.isLoading = false
      },
      error: (error) => {
        console.error("Error loading performance summary:", error)
        this.setError("Could not load performance summary data.")
        this.isLoading = false
      },
    })
  }

  updatePerformanceTrendChart(trendData: any[]): void {
    const labels = trendData.map((item) => {
      const date = new Date(item.date)
      return date.toLocaleDateString()
    })
    const data = trendData.map((item) => item.score)

    const chartData: ChartData = {
      labels: labels,
      datasets: [
        {
          data: data,
          label: "Interview Score",
          fill: false,
          tension: 0.1,
          backgroundColor: "rgba(75, 192, 192, 0.2)",
          borderColor: "rgb(75, 192, 192)",
          pointBackgroundColor: "rgb(75, 192, 192)",
          pointBorderColor: "#fff",
          pointHoverBackgroundColor: "#fff",
          pointHoverBorderColor: "rgb(75, 192, 192)",
        },
      ],
    }

    const canvas = document.getElementById("performanceTrendChart") as HTMLCanvasElement
    if (!canvas) {
      console.error('Canvas element with ID "performanceTrendChart" not found')
      return
    }

    const existingChart = Chart.getChart(canvas)
    if (existingChart) {
      console.log("Destroying existing performance trend chart before creating a new one")
      existingChart.destroy()
    }

    try {
      new Chart(canvas, {
        type: "line",
        data: chartData,
        options: this.lineChartOptions,
      })
      console.log("Performance trend chart created successfully")
    } catch (error) {
      console.error("Error creating performance trend chart:", error)
    }
  }

  updateTopicPerformanceChart(topicData: any[]): void {
    if (!topicData || topicData.length === 0) {
      console.log("No topic data available to display")
      return
    }

    const topTopics = topicData.slice(0, 8)

    const topicPerformanceData = {
      labels: topTopics.map((topic) => topic.topic),
      datasets: [
        {
          data: topTopics.map((topic) => topic.score),
          label: "Performance Score",
          backgroundColor: "rgba(75, 192, 192, 0.2)",
          borderColor: "rgb(75, 192, 192)",
          borderWidth: 1,
        },
      ],
    }

    const canvas = document.getElementById("topicPerformanceChart") as HTMLCanvasElement
    if (!canvas) {
      console.error('Canvas element with ID "topicPerformanceChart" not found')
      return
    }

    const existingChart = Chart.getChart(canvas)
    if (existingChart) {
      console.log("Destroying existing chart before creating a new one")
      existingChart.destroy()
    }

    // Create new chart
    try {
      new Chart(canvas, {
        type: "bar",
        data: topicPerformanceData,
        options: this.barChartOptions,
      })
      console.log("Topic performance chart created successfully")
    } catch (error) {
      console.error("Error creating topic performance chart:", error)
    }
  }


  processSkillData(skillData: PerformanceData[])
    : void
  {
    if (!skillData || skillData.length === 0) {
      console.log("No skill data to process")
      return;
    }

    const skills = skillData.map((skill) => skill.skillName || skill.skill)

    const latestScores = skillData.map((skill) => {
      if (!skill.scores || skill.scores.length === 0) {
        return skill.score
      }

      const sortedScores = [...skill.scores].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())

      return sortedScores[0].score
    })

    const previousScores = latestScores.map((score) => Math.max(0, score - Math.random() * 0.15))

    if (skills.length > 0) {
      this.skillsData = {
        labels: skills,
        datasets: [
          {
            data: latestScores,
            label: "Current Skills",
            backgroundColor: "rgba(99, 102, 241, 0.2)",
            borderColor: "rgb(99, 102, 241)",
            pointBackgroundColor: "rgb(99, 102, 241)",
            pointHoverBackgroundColor: "#fff",
            pointHoverBorderColor: "rgb(99, 102, 241)",
          },
          {
            data: previousScores,
            label: "Previous Assessment",
            backgroundColor: "rgba(209, 213, 219, 0.2)",
            borderColor: "rgb(209, 213, 219)",
            pointBackgroundColor: "rgb(209, 213, 219)",
            pointHoverBackgroundColor: "#fff",
            pointHoverBorderColor: "rgb(209, 213, 219)",
            borderDash: [5, 5],
          },
        ],
      }

      console.log("Updated skills radar chart with data:", {
        labels: skills,
        currentData: latestScores,
        previousData: previousScores,
      })
    }
  }
  updateSkillsRadarChart()
    : void
  {
    const canvas = document.getElementById("skillsRadarChart") as HTMLCanvasElement
    if (!canvas) {
      console.error('Canvas element with ID "skillsRadarChart" not found')
      return;
    }

    // Get existing chart instance if it exists
    const existingChart = Chart.getChart(canvas)
    if (existingChart) {
      console.log("Updating existing skills radar chart with new data")
      existingChart.data = this.skillsData
      existingChart.update()
    } else {
      console.log("Creating new skills radar chart")
      try {
        new Chart(canvas, {
          type: "radar",
          data: this.skillsData,
          options: this.radarChartOptions,
        })
      } catch (error) {
        console.error("Error creating skills radar chart:", error)
      }
    }
  }


  private processSkillImprovements(skillData: PerformanceData[]): void {
    if (!skillData || skillData.length === 0) {
      console.log("No skill improvement data to process")
      return
    }

    const colorMap: { [key: string]: string } = {
      "Problem Solving": "#8B5CF6",
      "System Design": "#EC4899",
      Communication: "#10B981",
      "Technical Knowledge": "#3B82F6",
      "Code Quality": "#F59E0B",
      default: "#6B7280",
    }

    this.skillImprovements = skillData.map((skill) => {
      const sortedScores = [...skill.scores].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())

      const latestScore = sortedScores.length > 0 ? sortedScores[0].score : 0
      const previousScore = sortedScores.length > 1 ? sortedScores[1].score : 0
      const improvement = latestScore - previousScore

      return {
        skill: skill.skillName,
        color: colorMap[skill.skillName] || colorMap["default"],
        score: latestScore,
        improvement: improvement,
      }
    })

    console.log("Updated skill improvements:", this.skillImprovements)
  }

  private processPerformanceImprovementData(skillData: PerformanceData[]): void {
    if (!skillData || skillData.length === 0) {
      console.log("No performance improvement data to process")
      return
    }

    const sessionsMap = new Map<
      string,
      {
        date: Date
        label: string
      }
    >()

    skillData.forEach((skill) => {
      skill.scores.forEach((score) => {
        const date = score.date instanceof Date ? score.date : new Date(score.date)
        const sessionId = score.sessionId.toString()

        if (!sessionsMap.has(sessionId)) {
          sessionsMap.set(sessionId, {
            date,
            label: `Interview ${sessionsMap.size + 1}`,
          })
        }
      })
    })

    // Sort sessions by date
    const sortedSessions = Array.from(sessionsMap.entries())
      .sort((a, b) => a[1].date.getTime() - b[1].date.getTime())
      .map((entry) => ({
        id: entry[0],
        label: entry[1].label,
        date: entry[1].date,
      }))

    const colorMap: { [key: string]: string } = {
      "Problem Solving": "#8B5CF6",
      "System Design": "#EC4899",
      Communication: "#10B981",
      "Technical Knowledge": "#3B82F6",
      "Code Quality": "#F59E0B",
      default: "#6B7280",
    }

    const datasets = skillData.map((skill) => {
      const color = colorMap[skill.skillName] || colorMap["default"]

      const data = sortedSessions.map((session) => {
        const score = skill.scores.find((s) => s.sessionId.toString() === session.id)
        return score ? score.score : null
      })

      return {
        data,
        label: skill.skillName,
        borderColor: color,
        backgroundColor: `${color}1A`,
        tension: 0.3,
        fill: false,
        pointBackgroundColor: color,
        pointBorderColor: "#FFF",
        pointHoverBackgroundColor: "#FFF",
        pointHoverBorderColor: color,
        pointRadius: 4,
        pointHoverRadius: 6,
      }
    })

    this.performanceImprovementData = {
      labels: sortedSessions.map((session) => session.label),
      datasets,
    }

    console.log("Updated performance improvement data:", this.performanceImprovementData)
  }

  filterInterviews(): void {
    console.log("Filtering interviews")
  }

  exportInterviews(): void {
    console.log("Exporting interviews")
  }

  private setError(message: string): void {
    this.hasError = true
    this.errorMessage = message
    console.error(message)
  }





  refreshDashboard()
    : void
  {
    console.log("Manually refreshing dashboard data")
    if (this.userId) {
      this.isLoading = true
      this.loadSkillsData()
      this.loadPerformanceSummary()
    } else {
      console.error("Cannot refresh: No user ID available")
    }
  }

  startAutoRefresh()
    : void
  {
    this.refreshTimer = setInterval(() => {
      if (!this.isLoading && this.userId) {
        console.log("Auto-refreshing skills data")
        this.loadSkillsData()
      }
    }, 30000)
  }
  private loadSampleData(): void {
    this.performanceData = [
      { month: "Jan", score: 75 },
      { month: "Feb", score: 82 },
      { month: "Mar", score: 78 },
      { month: "Apr", score: 85 },
      { month: "May", score: 92 },
    ]

    this.statsCards = [
      { icon: "award", title: "Success Rate", value: "85%", trend: "+5%", color: "blue" },
      { icon: "brain", title: "Total Interviews", value: "24", trend: "+2", color: "green" },
      { icon: "book-open", title: "Topics Covered", value: "12", trend: "+3", color: "purple" },
      { icon: "bar-chart", title: "Best Score", value: "92%", trend: "+8%", color: "yellow" },
    ]

    this.skillsData.labels = [
      "Technical Knowledge",
      "Communication",
      "Problem Solving",
      "System Design",
      "Code Quality",
    ]
    this.skillsData.datasets[0].data = [85, 90, 88, 82, 75]
    this.skillsData.datasets[1].data = [80, 85, 82, 75, 70]

    this.topicPerformanceData.labels = ["Algorithms", "Database", "System Design", "API Design", "Security"]
    this.topicPerformanceData.datasets[0].data = [88, 92, 75, 85, 78]

    this.skillImprovements = [
      { skill: "Problem Solving", color: "#8B5CF6", score: 90, improvement: 5 },
      { skill: "System Design", color: "#EC4899", score: 85, improvement: 7 },
      { skill: "Communication", color: "#10B981", score: 92, improvement: 4 },
      { skill: "Technical Knowledge", color: "#3B82F6", score: 88, improvement: 6 },
      { skill: "Code Quality", color: "#F59E0B", score: 86, improvement: 6 },
    ]

    this.performanceImprovementData = {
      labels: ["Interview 1", "Interview 2", "Interview 3", "Interview 4", "Latest"],
      datasets: [
        {
          data: [65, 72, 78, 85, 90],
          label: "Problem Solving",
          borderColor: "#8B5CF6",
          backgroundColor: "rgba(139, 92, 246, 0.1)",
          tension: 0.3,
          fill: false,
          pointBackgroundColor: "#8B5CF6",
          pointBorderColor: "#FFF",
          pointHoverBackgroundColor: "#FFF",
          pointHoverBorderColor: "#8B5CF6",
          pointRadius: 4,
          pointHoverRadius: 6,
        },
        {
          data: [55, 63, 70, 78, 85],
          label: "System Design",
          borderColor: "#EC4899",
          backgroundColor: "rgba(236, 72, 153, 0.1)",
          tension: 0.3,
          fill: false,
          pointBackgroundColor: "#EC4899",
          pointBorderColor: "#FFF",
          pointHoverBackgroundColor: "#FFF",
          pointHoverBorderColor: "#EC4899",
          pointRadius: 4,
          pointHoverRadius: 6,
        },
        {
          data: [70, 75, 82, 88, 92],
          label: "Communication",
          borderColor: "#10B981",
          backgroundColor: "rgba(16, 185, 129, 0.1)",
          tension: 0.3,
          fill: false,
          pointBackgroundColor: "#10B981",
          pointBorderColor: "#FFF",
          pointHoverBackgroundColor: "#FFF",
          pointHoverBorderColor: "#10B981",
          pointRadius: 4,
          pointHoverRadius: 6,
        },
        {
          data: [60, 68, 75, 82, 88],
          label: "Technical Knowledge",
          borderColor: "#3B82F6",
          backgroundColor: "rgba(59, 130, 246, 0.1)",
          tension: 0.3,
          fill: false,
          pointBackgroundColor: "#3B82F6",
          pointBorderColor: "#FFF",
          pointHoverBackgroundColor: "#FFF",
          pointHoverBorderColor: "#3B82F6",
          pointRadius: 4,
          pointHoverRadius: 6,
        },
        {
          data: [58, 65, 73, 80, 86],
          label: "Code Quality",
          borderColor: "#F59E0B",
          backgroundColor: "rgba(245, 158, 11, 0.1)",
          tension: 0.3,
          fill: false,
          pointBackgroundColor: "#F59E0B",
          pointBorderColor: "#FFF",
          pointHoverBackgroundColor: "#FFF",
          pointHoverBorderColor: "#F59E0B",
          pointRadius: 4,
          pointHoverRadius: 6,
        },
      ],
    }

    this.isLoading = false
  }

}

