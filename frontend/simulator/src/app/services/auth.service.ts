import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ForgotPasswordRequest} from '../models/forgot-password-request';
import {ResetPasswordRequest} from '../models/reset-password-request';

@Injectable({
  providedIn: 'root'
})

export class AuthService {
  private baseUrl = 'http://localhost:8083/api/v1/auth';


  constructor( private http:HttpClient) { }
  forgotPassword(email: string):Observable<void> {
    const request: ForgotPasswordRequest = {email};
    return this.http.post<void>(`${this.baseUrl}/forgot-password`, request);
  }
  resetPassword(token:string , newPassword : string): Observable<void>{
    const request: ResetPasswordRequest={token  ,newPassword};
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

}
