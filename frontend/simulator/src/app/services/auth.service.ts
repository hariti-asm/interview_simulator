import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, tap, BehaviorSubject, catchError, throwError} from 'rxjs';
import { ForgotPasswordRequest } from '../models/forgot-password-request';
import { ResetPasswordRequest } from '../models/reset-password-request';
import {AuthResponse} from '../models/auth-response';
import {LoginRequest} from '../models/login-request';
import {RegisterRequest} from '../models/register-request';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8083/api/v1/auth';

  private authStateSubject = new BehaviorSubject<boolean>(this.isTokenValid());
  public authStateChanged = this.authStateSubject.asObservable();

  private userProfileSubject = new BehaviorSubject<any>(null);
  public userProfile = this.userProfileSubject.asObservable();

  constructor(private http: HttpClient) {
    this.authStateSubject.next(this.isTokenValid());
  }

  login(email: string, password: string, rememberMe: boolean): Observable<AuthResponse> {
    const loginRequest: LoginRequest = {
      email,
      password,
      rememberMe
    };

    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
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
          this.authStateSubject.next(true);
          this.fetchUserProfile();
        }
      })
    );
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, request, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  logout(): Observable<any> {
    const refreshToken = localStorage.getItem('refresh_token');

    return this.http.post(`${this.baseUrl}/logout`, { refreshToken }).pipe(
      tap(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('refresh_token');
        this.authStateSubject.next(false);
        this.userProfileSubject.next(null);
      }),
      catchError(error => {
        localStorage.removeItem('token');
        localStorage.removeItem('refresh_token');
        this.authStateSubject.next(false);
        this.userProfileSubject.next(null);
        return throwError(error);
      })
    );
  }

  isAuthenticated(): boolean {
    return this.isTokenValid();
  }

  private isTokenValid(): boolean {
    const token = this.getToken();
    if (!token) return false;
    return !this.isTokenExpired();
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
    return this.userProfile;
  }

  private fetchUserProfile(): void {
    const token = this.getToken();
    if (!token) {
      this.userProfileSubject.next(null);
      return;
    }

    const httpOptions = {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${token}`
      })
    };

    this.http.get<any>(`${this.baseUrl}/profile`, httpOptions)
      .subscribe(
        (profile) => {
          this.userProfileSubject.next(profile);
        },
        (error) => {
          console.error('Error fetching user profile:', error);
          if (error.status === 401) {
            this.removeToken();
            this.authStateSubject.next(false);
            this.userProfileSubject.next(null);
          }
        }
      );
  }
  updateUserProfile(profileData: any): Observable<any> {
    const token = this.getToken();
    if (!token) {
      return new Observable(observer => {
        observer.error('Not authenticated');
        observer.complete();
      });
    }

    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      })
    };

    return this.http.put<any>(`${this.baseUrl}/profile`, profileData, httpOptions)
      .pipe(
        tap(response => {
          this.userProfileSubject.next({
            ...this.userProfileSubject.value,
            ...profileData
          });
        })
      );
}}
