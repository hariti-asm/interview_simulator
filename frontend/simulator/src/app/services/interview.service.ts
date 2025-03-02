import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {InterviewSessionDTO} from '../models/interview-sessiondto';
import {QuestionDTO} from '../models/questiondto';
import {AnswerDTO} from '../models/answerdto';
import {PerformanceData} from '../models/performance-data';
import {AuthService} from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class InterviewService {
  private apiUrl = "http://localhost:8083/api/interview";
  private userUrl = "http://localhost:8083/api/users";

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getAuthHeaders() {
    const token = this.authService.getToken();
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      })
    };
  }

  startInterview(position: string, specialization: string, experienceLevel: string, userId?: {
    headers: HttpHeaders
  }): Observable<any> {
    let params = new HttpParams()
      .set('position', position)
      .set('specialization', specialization)
      .set('experienceLevel', experienceLevel);

    if (userId) {
      params = params.set('userId', userId.toString());
    }

    console.log('Starting interview with params:', params.toString());
    console.log('Using authorization token:', this.authService.getToken());

    const requestBody = userId ? { userId } : null;

    return this.http.post(`${this.apiUrl}/start`, requestBody, {
      ...this.getAuthHeaders(),
      params
    });
  }

  getNextQuestion(sessionId: number): Observable<QuestionDTO> {
    const params = new HttpParams()
      .set('sessionId', sessionId.toString());

    return this.http.get<QuestionDTO>(`${this.apiUrl}/next-question`, {
      ...this.getAuthHeaders(),
      params
    });
  }

  processAnswer(sessionId: number, questionId: number, answer: string): Observable<AnswerDTO> {
    const params = new HttpParams()
      .set('sessionId', sessionId.toString())
      .set('questionId', questionId.toString())
      .set('answer', answer);

    return this.http.post<AnswerDTO>(`${this.apiUrl}/process-answer`, null, {
      ...this.getAuthHeaders(),
      params
    });
  }

  getRecentInterviews(): Observable<any> {
    return this.http.get(`${this.apiUrl}/recent`, this.getAuthHeaders());
  }

  getUserInterviews(userId: number): Observable<InterviewSessionDTO[]> {
    return this.http.get<InterviewSessionDTO[]>(`${this.userUrl}/${userId}/interviews`, this.getAuthHeaders());
  }

  getInterviewSessions(userId: number): Observable<InterviewSessionDTO[]> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<InterviewSessionDTO[]>(`${this.apiUrl}/sessions`, {
      ...this.getAuthHeaders(),
      params
    });
  }

  getUserPerformance(userId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${userId}/performance`, this.getAuthHeaders());
  }

  getPerformanceBySkill(userId: number): Observable<PerformanceData[]> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<PerformanceData[]>(`${this.apiUrl}/performance/skills`, {
      ...this.getAuthHeaders(),
      params
    });
  }

  getOverallPerformanceData(userId: number): Observable<any> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<any>(`${this.apiUrl}/performance/summary`, {
      ...this.getAuthHeaders(),
      params
    });
  }

  getSessionDetails(sessionId: number): Observable<InterviewSessionDTO> {
    return this.http.get<InterviewSessionDTO>(`${this.apiUrl}/${sessionId}`, this.getAuthHeaders());
  }

  getSessionQuestions(sessionId: number): Observable<QuestionDTO[]> {
    const params = new HttpParams().set('sessionId', sessionId.toString());
    return this.http.get<QuestionDTO[]>(`${this.apiUrl}/session/${sessionId}/questions`, {
      ...this.getAuthHeaders(),
      params
    });
  }

  exportSessionResults(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}/export`, {
      ...this.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  getInterviewFeedback(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}/feedback`, this.getAuthHeaders());
  }

  deleteInterview(sessionId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/sessions/${sessionId}`, this.getAuthHeaders());
  }

  getInterviewById(sessionId: number): Observable<InterviewSessionDTO> {
    return this.http.get<InterviewSessionDTO>(`${this.apiUrl}/${sessionId}`, this.getAuthHeaders());
  }
}
