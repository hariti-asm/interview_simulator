import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {InterviewSessionDTO} from '../models/interview-sessiondto';
import {AnswerDTO} from '../models/answerdto';
import {QuestionDTO} from '../models/questiondto';
import {PerformanceData} from '../models/performance-data';

@Injectable({
  providedIn: 'root'
})
export class AIInterviewService {
  private apiUrl = 'http://localhost:8083/api/interview';

  constructor(private http: HttpClient) {}

  startNewSession(sessionData: {
    userId: number;
    position: string;
    specialization: string;
    experienceLevel: string;
    questionCount: number;
  }): Observable<InterviewSessionDTO> {
    return this.http.post<InterviewSessionDTO>(`${this.apiUrl}/sessions/new`, sessionData);
  }


  processAnswer(
    userId: number,
    sessionId: number,
    questionId: number,
    answer: string
  ): Observable<AnswerDTO> {
    return this.http.post<AnswerDTO>(`${this.apiUrl}/sessions/${sessionId}/answers`, {
      userId,
      sessionId,
      questionId,
      answerText: answer
    });
  }

  generateNextQuestion(
    userId: number,
    sessionId: number
  ): Observable<QuestionDTO> {
    return this.http.get<QuestionDTO>(
      `${this.apiUrl}/sessions/${sessionId}/next-question?userId=${userId}`
    );
  }

  getInterviewSessions(userId: number): Observable<InterviewSessionDTO[]> {
    return this.http.get<InterviewSessionDTO[]>(
      `${this.apiUrl}/sessions?userId=${userId}`
    );
  }

  getSessionPerformance(sessionId: number): Observable<AnswerDTO[]> {
    return this.http.get<AnswerDTO[]>(
      `${this.apiUrl}/sessions/${sessionId}/answers`
    );
  }

  getPerformanceBySkill(userId: number): Observable<PerformanceData[]> {
    return this.http.get<PerformanceData[]>(
      `${this.apiUrl}/performance/${userId}/by-skill`
    );
  }

  getOverallPerformanceData(userId: number): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/performance/${userId}/summary`
    );
  }
}
