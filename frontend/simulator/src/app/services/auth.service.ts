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
    // Try to load profile on service initialization if token exists
    if (this.isTokenValid()) {
      this.fetchUserProfile();
    }
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
          // Save refresh token if available
          if (response.refreshToken) {
            localStorage.setItem('refresh_token', response.refreshToken);
          }
          this.authStateSubject.next(true);
          // After login, fetch the user profile
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
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  logout(): Observable<any> {
    const refreshToken = localStorage.getItem('refresh_token');

    // If no refresh token, just clear local storage
    if (!refreshToken) {
      this.clearAuthData();
      return of(null);
    }

    return this.http.post(`${this.baseUrl}/logout`, { refreshToken }).pipe(
      tap(() => {
        this.clearAuthData();
      }),
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
    return this.isTokenValid();
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
    // If we already have a profile, return it
    if (this.userProfileSubject.value) {
      return this.userProfile;
    }

    // Otherwise, fetch it
    this.fetchUserProfile();
    return this.userProfile;
  }

// Modify your fetchUserProfile method in auth.service.ts
  private fetchUserProfile(): void {
    const token = this.getToken();
    if (!token) {
      this.userProfileSubject.next(null);
      return;
    }

    console.log('Fetching user profile with token:', token.substring(0, 20) + '...');

    // Use the HttpClient with explicit headers
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    // Log the full request to debug
    console.log('Making request to:', `${this.baseUrl}/profile`);
    console.log('With headers:', headers);

    this.http.get<any>(`${this.baseUrl}/profile`, { headers, withCredentials: true })
      .subscribe({
        next: (profile) => {
          console.log('Profile fetched successfully:', profile);
          this.userProfileSubject.next(profile);
        },
        error: (error) => {
          console.error('Error fetching user profile:', error);
          // Log more details about the error
          console.error('Status:', error.status);
          console.error('Status Text:', error.statusText);
          console.error('Error Body:', error.error);

          if (error.status === 401) {
            console.log('Unauthorized. Clearing auth data.');
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
