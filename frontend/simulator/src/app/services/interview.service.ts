import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {InterviewSessionDTO} from '../models/interview-sessiondto';
import {QuestionDTO} from '../models/questiondto';
import {AnswerDTO} from '../models/answerdto';
import {PerformanceData} from '../models/performance-data';
import {AuthService} from './auth.service';
import {SelectedSkill} from '../models/selected-skill';

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

  startInterview(
    position: string,
    specialization: string,
    experienceLevel: string,
    skills: SelectedSkill[],
    userId?: string
  ): Observable<any> {
    let params = new HttpParams()
      .set('position', position)
      .set('specialization', specialization)
      .set('experienceLevel', experienceLevel);

    if (userId) {
      params = params.set('userId', userId);
    }

    console.log('Starting interview with params:', params.toString());
    console.log('Using authorization token:', this.authService.getToken());

    const requestBody = {
      userId: userId || null,
      skills: skills
    };

    return this.http.post(`${this.apiUrl}/start`, requestBody, {
      ...this.getAuthHeaders(),
      params
    });
  }

  getNextQuestion(sessionId: number, p0: { excludedQuestions: string[]; }): Observable<QuestionDTO> {
    const params = new HttpParams()
      .set('sessionId', sessionId.toString());

    return this.http.get<QuestionDTO>(`${this.apiUrl}/next-question`, {
      ...this.getAuthHeaders(),
      params
    });
  }

  getUserInterviews(userId: number): Observable<InterviewSessionDTO[]> {
    return this.http.get<InterviewSessionDTO[]>(`${this.userUrl}/${userId}/interviews`).pipe(
      map(sessions =>
        sessions.map(session => {
          if (session.questions && session.questions.length > 0) {
            const questions = session.questions as QuestionDTO[];

            const totalScore = questions.reduce((sum, question) => {
              const questionScore = question.answer?.score ?? 0;
              console.log(`Question ${question.id} score: ${questionScore}`);

              return sum + questionScore;
            }, 0);

            session.score = questions.length > 0 ? totalScore / questions.length : 0;

            if (session.score <= 10 && session.score > 0) {
              session.score = session.score * 10;
            }
          } else {
            session.score = 0;
          }

          return session;
        })
      )
    );
  }


  getPerformanceBySkill(): Observable<PerformanceData[]> {
    return this.http.get<PerformanceData[]>(`${this.apiUrl}/performance/skills`, this.getAuthHeaders());
  }

  getOverallPerformanceData(userId: number): Observable<any> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<any>(
      `${this.apiUrl}/performance/summary`,
      {
        ...this.getAuthHeaders(),
        params
      }
    );
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
  submitAnswer(sessionId: number, questionId: number, answerData: AnswerDTO): Observable<AnswerDTO> {
    return this.http.post<AnswerDTO>(
      `${this.apiUrl}/sessions/${sessionId}/questions/${questionId}/answers`,
      answerData,
      { headers: this.authService.getAuthHeaders() }
    );
  }
  getInterviewPositionCounts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/positions/count`, this.getAuthHeaders());
  }


  getUserSkillPerformance(userId: number): Observable<PerformanceData[]> {
    return this.http.get<PerformanceData[]>(`${this.apiUrl}/performance/skills/${userId}`);
  }
}
