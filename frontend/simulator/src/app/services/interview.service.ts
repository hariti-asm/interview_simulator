import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InterviewSessionDTO } from '../models/interview-sessiondto';
import { QuestionDTO } from '../models/questiondto';
import { AnswerDTO } from '../models/answerdto';
import { PerformanceData } from '../models/performance-data';

@Injectable({
  providedIn: 'root'
})
export class InterviewService {
  private apiUrl = "http://localhost:8083/api/interview";

  constructor(private http: HttpClient) {}

  startInterview(position: string, specialization: string, experienceLevel: string): Observable<InterviewSessionDTO> {
    const params = new HttpParams()
      .set('position', position)
      .set('specialization', specialization)
      .set('experienceLevel', experienceLevel);

    return this.http.post<InterviewSessionDTO>(`${this.apiUrl}/start`, null, { params });
  }

  getNextQuestion(sessionId: number): Observable<QuestionDTO> {
    const params = new HttpParams()
      .set('sessionId', sessionId.toString());

    return this.http.get<QuestionDTO>(`${this.apiUrl}/next-question`, { params });
  }

  processAnswer(sessionId: number, questionId: number, answer: string): Observable<AnswerDTO> {
    const params = new HttpParams()
      .set('sessionId', sessionId.toString())
      .set('questionId', questionId.toString())
      .set('answer', answer);

    return this.http.post<AnswerDTO>(`${this.apiUrl}/process-answer`, null, { params });
  }

  getRecentInterviews(): Observable<any> {
    return this.http.get(`${this.apiUrl}/recent`);
  }

  getInterviewSessions(userId: number): Observable<InterviewSessionDTO[]> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<InterviewSessionDTO[]>(`${this.apiUrl}/sessions`, { params });
  }

  getPerformanceBySkill(userId: number): Observable<PerformanceData[]> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<PerformanceData[]>(`${this.apiUrl}/performance/skills`, { params });
  }

  getOverallPerformanceData(userId: number): Observable<any> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<any>(`${this.apiUrl}/performance/summary`, { params });
  }

  // Additional utility methods
  getSessionDetails(sessionId: number): Observable<InterviewSessionDTO> {
    return this.http.get<InterviewSessionDTO>(`${this.apiUrl}/session/${sessionId}`);
  }

  getSessionQuestions(sessionId: number): Observable<QuestionDTO[]> {
    const params = new HttpParams().set('sessionId', sessionId.toString());
    return this.http.get<QuestionDTO[]>(`${this.apiUrl}/session/${sessionId}/questions`, { params });
  }

  exportSessionResults(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}/export`, {
      responseType: 'blob'
    });
  }

  getInterviewFeedback(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}/feedback`);
  }
}
