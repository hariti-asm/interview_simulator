import { Injectable } from "@angular/core"
import {  HttpClient, HttpHeaders } from "@angular/common/http"
import { Observable, catchError, throwError, map } from "rxjs"
import  { InterviewSessionDTO } from "../models/interview-sessiondto"
import  { QuestionDTO } from "../models/questiondto"
import  { AnswerDTO } from "../models/answerdto"
import  { PerformanceData } from "../models/performance-data"
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

    return this.http.post(`${this.apiUrl}/start`, requestBody, {
      ...this.getAuthHeaders(),
      params,
    })
  }

  getNextQuestion(sessionId: number, options: { excludedQuestions: string[] }): Observable<QuestionDTO> {
    const params = {
      sessionId: sessionId.toString(),
    }

    return this.http.get<QuestionDTO>(`${this.apiUrl}/next-question`, {
      ...this.getAuthHeaders(),
      params,
    })
  }

  getUserInterviews(userId: number): Observable<InterviewSessionDTO[]> {
    console.log(`Fetching interviews for user ${userId} with auth headers`)

    // Fixed: Use the getAuthHeaders() method which returns the correct structure
    return this.http.get<InterviewSessionDTO[]>(`${this.userUrl}/${userId}/interviews`, this.getAuthHeaders()).pipe(
      map((sessions) => {
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
    return this.http.get<PerformanceData[]>(`${this.apiUrl}/performance/skills`, this.getAuthHeaders())
  }

  getOverallPerformanceData(userId: number): Observable<any> {
    const params = {
      userId: userId.toString(),
    }

    return this.http.get<any>(`${this.apiUrl}/performance/summary`, {
      ...this.getAuthHeaders(),
      params,
    })
  }

  getSessionDetails(sessionId: number): Observable<InterviewSessionDTO> {
    return this.http.get<InterviewSessionDTO>(`${this.apiUrl}/${sessionId}`, this.getAuthHeaders())
  }

  getSessionQuestions(sessionId: number): Observable<QuestionDTO[]> {
    return this.http.get<QuestionDTO[]>(`${this.apiUrl}/session/${sessionId}/questions`, this.getAuthHeaders())
  }

  exportSessionResults(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}/export`, {
      ...this.getAuthHeaders(),
      responseType: "blob",
    })
  }

  getInterviewFeedback(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}/feedback`, this.getAuthHeaders())
  }

  deleteInterview(sessionId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/sessions/${sessionId}`, this.getAuthHeaders())
  }

  getInterviewById(sessionId: number): Observable<InterviewSessionDTO> {
    return this.http.get<InterviewSessionDTO>(`${this.apiUrl}/${sessionId}`, this.getAuthHeaders())
  }

  submitAnswer(sessionId: number, questionId: number, answerData: AnswerDTO): Observable<AnswerDTO> {
    return this.http.post<AnswerDTO>(
      `${this.apiUrl}/sessions/${sessionId}/questions/${questionId}/answers`,
      answerData,
      this.getAuthHeaders(),
    )
  }

  getInterviewPositionCounts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/positions/count`, this.getAuthHeaders())
  }

  getUserSkillPerformance(userId: number): Observable<PerformanceData[]> {
    console.log(`Fetching skill performance for user ${userId} with auth headers`)
    return this.http.get<PerformanceData[]>(`${this.apiUrl}/performance/skills/${userId}`, this.getAuthHeaders())
  }
}

