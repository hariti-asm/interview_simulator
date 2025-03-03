import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap, BehaviorSubject, catchError, throwError, of } from 'rxjs';
import { ForgotPasswordRequest } from '../models/forgot-password-request';
import { ResetPasswordRequest } from '../models/reset-password-request';
import { AuthResponse } from '../models/auth-response';
import { LoginRequest } from '../models/login-request';
import { RegisterRequest } from '../models/register-request';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8083/api/v1/auth';

  authStateSubject = new BehaviorSubject<boolean>(this.isTokenValid());
  public authStateChanged = this.authStateSubject.asObservable();

  private userProfileSubject = new BehaviorSubject<any>(null);
  public userProfile = this.userProfileSubject.asObservable();

  constructor(private http: HttpClient) {
    this.authStateSubject.next(this.isTokenValid());
    if (this.isTokenValid()) {
      this.fetchUserProfile();
    }
  }

  login(email: string, password: string, rememberMe: boolean): Observable<AuthResponse> {
    const loginRequest: LoginRequest = { email, password, rememberMe };
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      withCredentials: true
    };

    return this.http.post<AuthResponse>(
      `${this.baseUrl}/login`,
      loginRequest,
      httpOptions
    ).pipe(
      tap(response => {
        if (response.token) {
          this.saveToken(response.token);
          if (response.refreshToken) {
            localStorage.setItem('refresh_token', response.refreshToken);
          }
          this.authStateSubject.next(true);
          this.fetchUserProfile();
        }
      }),
      catchError(error => {
        console.error('Login failed:', error);
        return throwError(error);
      })

    );
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, request, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  logout(): Observable<any> {
    const refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) {
      this.clearAuthData();
      return of(null);
    }
    return this.http.post(`${this.baseUrl}/logout`, { refreshToken }).pipe(
      tap(() => this.clearAuthData()),
      catchError(error => {
        this.clearAuthData();
        return throwError(error);
      })
    );
  }

  private clearAuthData(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('refresh_token');
    this.authStateSubject.next(false);
    this.userProfileSubject.next(null);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token && !this.isTokenExpired();
  }

  private isTokenValid(): boolean {
    const token = this.getToken();
    if (!token) return false;
    return !this.isTokenExpired();
  }

  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  forgotPassword(email: string): Observable<void> {
    const request: ForgotPasswordRequest = { email };
    return this.http.post<void>(`${this.baseUrl}/forgot-password`, request);
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    const request: ResetPasswordRequest = { token, newPassword };
    return this.http.post<void>(`${this.baseUrl}/reset-password`, request);
  }

  saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  removeToken(): void {
    localStorage.removeItem('token');
  }

  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp ? payload.exp * 1000 < Date.now() : false;
    } catch (e) {
      return true;
    }
  }


  getUserProfile(): Observable<any> {
    if (this.userProfileSubject.value) {
      return this.userProfile;
    }
    this.fetchUserProfile();
    return this.userProfile;
  }

  private fetchUserProfile(): void {
    const token = this.getToken();
    if (!token) {
      this.userProfileSubject.next(null);
      return;
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    this.http.get<any>(`${this.baseUrl}/profile`, { headers, withCredentials: true })
      .subscribe({
        next: (profile) => {
          this.userProfileSubject.next(profile);
        },
        error: (error) => {
          if (error.status === 401) {
            this.clearAuthData();
          }
        }
      });
  }

  updateUserProfile(profileData: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/profile`, profileData)
      .pipe(
        tap(response => {
          this.userProfileSubject.next({
            ...this.userProfileSubject.value,
            ...profileData
          });
        })
      );
  }
}
