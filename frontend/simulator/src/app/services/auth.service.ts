import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ForgotPasswordRequest } from '../models/forgot-password-request';
import { ResetPasswordRequest } from '../models/reset-password-request';
import {AuthResponse} from '../models/auth-response';
import {LoginRequest} from '../models/login-request';
import {Role} from '../models/role.enum';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8083/api/v1/auth';

  constructor(private http: HttpClient) { }

  login(email: string, password: string): Observable<AuthResponse> {
    const request: LoginRequest = { email, password };
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request)
      .pipe(
        tap(response => {
          if (response.token) {
            this.saveToken(response.token);
          }
        })
      );
  }

  register(name: string, email: string, password: string, role: Role): Observable<AuthResponse> {
    const request: { password: string; role: Role; name: string; email: string } = { name, email, password, role };
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request)
      .pipe(
        tap(response => {
          if (response.token) {
            this.saveToken(response.token);
          }
        })
      );
  }

  logout(): void {
    this.removeToken();
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
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
}
