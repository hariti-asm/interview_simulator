import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UserDTO } from '../models/userdto';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8083/api/users'; // Backend URL

  private http = inject(HttpClient);
  private authService = inject(AuthService);

  /**
   * Get authentication headers for API requests
   * @returns HTTP options with auth headers
   */
  private getAuthHeaders() {
    const token = this.authService.getToken();
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      })
    };
  }

  getAllUsers(): Observable<UserDTO[]> {
    console.log('Fetching all users with auth headers');
    return this.http.get<UserDTO[]>(this.apiUrl, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error('Error fetching all users:', error);
        return throwError(() => error);
      })
    );
  }

  getUserById(id: number): Observable<UserDTO> {
    console.log(`Fetching user ${id} with auth headers`);
    return this.http.get<UserDTO>(`${this.apiUrl}/${id}`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error fetching user ${id}:`, error);
        return throwError(() => error);
      })
    );
  }

  createUser(user: UserDTO): Observable<UserDTO> {
    console.log('Creating user with auth headers:', user);
    return this.http.post<UserDTO>(this.apiUrl, user, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error('Error creating user:', error);
        return throwError(() => error);
      })
    );
  }

  updateUser(id: number | undefined, user: UserDTO): Observable<UserDTO> {
    if (id === undefined || id === null) {
      return throwError(() => new Error('Invalid user ID: ID cannot be undefined or null'));
    }

    console.log(`Updating user ${id} with auth headers:`, user);
    return this.http.put<UserDTO>(`${this.apiUrl}/${id}`, user, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error updating user ${id}:`, error);
        return throwError(() => error);
      })
    );
  }

  deleteUser(id: number): Observable<void> {
    console.log(`Deleting user ${id} with auth headers`);
    return this.http.delete<void>(`${this.apiUrl}/${id}`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error deleting user ${id}:`, error);
        return throwError(() => error);
      })
    );
  }
}
