// interview.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {InterviewSessionDTO} from '../models/interview-sessiondto';
import {QuestionDTO} from '../models/questiondto';
import {AnswerDTO} from '../models/answerdto';

@Injectable({
  providedIn: 'root'
})
export class InterviewService {
  private apiUrl ="http://localhost:8083/api/interview";

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

  getRecentInterviews(): Observable<InterviewSessionDTO[]> {
    return this.http.get<InterviewSessionDTO[]>(`${this.apiUrl}/recent`);
  }
}
