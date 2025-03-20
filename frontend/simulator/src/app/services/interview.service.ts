import { Injectable } from "@angular/core"
import {  HttpClient, HttpHeaders } from "@angular/common/http"
import {  Observable, catchError, throwError, map, of } from "rxjs"
import { InterviewSessionDTO } from "../models/interview-sessiondto"
import  { QuestionDTO } from "../models/questiondto"
import { AnswerDTO } from "../models/answerdto"
import { PerformanceData } from "../models/performance-data"
import  { AuthService } from "./auth.service"
import  { SelectedSkill } from "../models/selected-skill"

@Injectable({
  providedIn: "root",
})
export class InterviewService {
  private apiUrl = "http://localhost:8083/api/interview"
  private userUrl = "http://localhost:8083/api/users"

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  private getAuthHeaders() {
    const token = this.authService.getToken()
    return {
      headers: new HttpHeaders({
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      }),
    }
  }

  startInterview(
    position: string,
    specialization: string,
    experienceLevel: string,
    skills: SelectedSkill[],
    userId?: string,
  ): Observable<any> {
    const params = {
      position,
      specialization,
      experienceLevel,
      ...(userId ? { userId } : {}),
    }

    console.log("Starting interview with params:", params)
    console.log("Using authorization token:", this.authService.getToken())

    const requestBody = {
      userId: userId || null,
      skills: skills,
    }

    return this.http
      .post(`${this.apiUrl}/start`, requestBody, {
        ...this.getAuthHeaders(),
        params,
      })
      .pipe(
        catchError((error) => {
          console.error("Error starting interview:", error)
          return throwError(() => error)
        }),
      )
  }

  getNextQuestion(sessionId: number, options: { excludedQuestions: string[] }): Observable<QuestionDTO> {
    const params = {
      sessionId: sessionId.toString(),
    }

    return this.http
      .get<QuestionDTO>(`${this.apiUrl}/next-question`, {
        ...this.getAuthHeaders(),
        params,
      })
      .pipe(
        catchError((error) => {
          console.error("Error getting next question:", error)
          return throwError(() => error)
        }),
      )
  }

  getUserInterviews(userId: number): Observable<InterviewSessionDTO[]> {
    console.log(`Fetching interviews for user ${userId} with auth headers`)

    return this.http.get<InterviewSessionDTO[]>(`${this.userUrl}/${userId}/interviews`, this.getAuthHeaders()).pipe(
      map((sessions) => {
        if (!sessions || sessions.length === 0) {
          console.log("No interview sessions found")
          return []
        }

        console.log(`Found ${sessions.length} interview sessions`)

        return sessions.map((session) => {
          if (session.questions && session.questions.length > 0) {
            const questions = session.questions as QuestionDTO[]

            const totalScore = questions.reduce((sum, question) => {
              const questionScore = question.answer?.score ?? 0
              console.log(`Question ${question.id} score: ${questionScore}`)

              return sum + questionScore
            }, 0)

            session.score = questions.length > 0 ? totalScore / questions.length : 0

            if (session.score <= 10 && session.score > 0) {
              session.score = session.score * 10
            }
          } else {
            session.score = 0
          }

          return session
        })
      }),
      catchError((error) => {
        console.error("Error fetching user interviews:", error)
        return throwError(() => error)
      }),
    )
  }

  getPerformanceBySkill(): Observable<PerformanceData[]> {
    return this.http.get<PerformanceData[]>(`${this.apiUrl}/performance/skills`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error("Error getting performance by skill:", error)
        return throwError(() => error)
      }),
    )
  }

  getOverallPerformanceData(userId: number): Observable<any> {
    const params = {
      userId: userId.toString(),
    }

    return this.http
      .get<any>(`${this.apiUrl}/performance/summary`, {
        ...this.getAuthHeaders(),
        params,
      })
      .pipe(
        catchError((error) => {
          console.error("Error getting overall performance data:", error)
          return throwError(() => error)
        }),
      )
  }

  getSessionDetails(sessionId: number): Observable<InterviewSessionDTO> {
    return this.http.get<InterviewSessionDTO>(`${this.apiUrl}/${sessionId}`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error getting session details for session ${sessionId}:`, error)
        return throwError(() => error)
      }),
    )
  }

  getSessionQuestions(sessionId: number): Observable<QuestionDTO[]> {
    return this.http.get<QuestionDTO[]>(`${this.apiUrl}/session/${sessionId}/questions`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error getting questions for session ${sessionId}:`, error)
        return throwError(() => error)
      }),
    )
  }

  exportSessionResults(sessionId: number): Observable<any> {
    return this.http
      .get(`${this.apiUrl}/session/${sessionId}/export`, {
        ...this.getAuthHeaders(),
        responseType: "blob",
      })
      .pipe(
        catchError((error) => {
          console.error(`Error exporting results for session ${sessionId}:`, error)
          return throwError(() => error)
        }),
      )
  }

  getInterviewFeedback(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}/feedback`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error getting feedback for session ${sessionId}:`, error)
        return throwError(() => error)
      }),
    )
  }

  deleteInterview(sessionId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/sessions/${sessionId}`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error deleting session ${sessionId}:`, error)
        return throwError(() => error)
      }),
    )
  }

  getInterviewById(sessionId: number): Observable<InterviewSessionDTO> {
    return this.http.get<InterviewSessionDTO>(`${this.apiUrl}/${sessionId}`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error getting interview ${sessionId}:`, error)
        return throwError(() => error)
      }),
    )
  }

  submitAnswer(sessionId: number, questionId: number, answerData: AnswerDTO): Observable<AnswerDTO> {
    console.log(`Submitting answer for session ${sessionId}, question ${questionId}:`, answerData)

    return this.http
      .post<AnswerDTO>(
        `${this.apiUrl}/sessions/${sessionId}/questions/${questionId}/answers`,
        answerData,
        this.getAuthHeaders(),
      )
      .pipe(
        map((response) => {
          console.log("Answer submission successful:", response)
          return response
        }),
        catchError((error) => {
          console.error("Error submitting answer:", error)
          return throwError(() => error)
        }),
      )
  }

  getInterviewPositionCounts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/positions/count`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error("Error getting interview position counts:", error)
        return throwError(() => error)
      }),
    )
  }

  getUserSkillPerformance(userId: number): Observable<PerformanceData[]> {
    console.log(`Fetching skill performance for user ${userId} with auth headers`)

    return this.http.get<PerformanceData[]>(`${this.apiUrl}/performance/skills/${userId}`, this.getAuthHeaders()).pipe(
      map((data) => {
        if (!data || data.length === 0) {
          console.log("No skills data available")
          return []
        }

        // Log the full data for debugging
        console.log("Skills data loaded successfully:", JSON.stringify(data))

        // Ensure all required properties are set and properly formatted
        return data.map((skill) => {
          // Make sure score is a number between 0 and 1
          if (typeof skill.score !== "number") {
            skill.score = 0
          }

          // Ensure skillName is set
          if (!skill.skillName) {
            skill.skillName = skill.skill
          }

          // Ensure scores array is properly formatted
          if (!skill.scores) {
            skill.scores = []
          } else {
            // Convert date strings to Date objects if needed
            skill.scores = skill.scores.map((score) => ({
              ...score,
              date: score.date instanceof Date ? score.date : new Date(score.date),
            }))
          }

          return skill
        })
      }),
      catchError((error) => {
        console.error("Error fetching skill performance:", error)
        // Return empty array instead of throwing to avoid breaking the UI
        return of([])
      }),
    )
  }
}

