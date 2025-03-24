import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { UserDTO } from '../models/userdto';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8083/api/users';

  private http = inject(HttpClient);
  private authService = inject(AuthService);


  private getAuthHeaders() {
    const token = this.authService.getToken();

    console.log('Using token:', token ? `${token.substring(0, 15)}...` : 'No token found');

    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      })
    };
  }


  private handleError(error: HttpErrorResponse, operation: string) {
    console.error(`Error during ${operation}:`, error);

    if (error.status === 403) {
      console.error('Authorization error: You may not have permission for this action');
      console.error('Headers sent:', error.headers);

      const token = this.authService.getToken();
      if (token) {
        try {
          const tokenData = JSON.parse(atob(token.split('.')[1]));
          const expiry = tokenData.exp * 1000;
          const now = Date.now();

          if (expiry < now) {
            console.error('Token is expired. Expiry:', new Date(expiry), 'Now:', new Date(now));
          } else {
            console.log('Token not expired. Roles/permissions:', tokenData.roles || tokenData.authorities || 'No roles found in token');
          }
        } catch (e) {
          console.error('Error parsing token:', e);
        }
      }
    }

    return throwError(() => error);
  }

  getAllUsers(): Observable<UserDTO[]> {
    console.log('Fetching all users with auth headers');
    return this.http.get<UserDTO[]>(this.apiUrl, this.getAuthHeaders()).pipe(
      catchError((error) => this.handleError(error, 'fetching all users'))
    );
  }

  getUserById(id: number): Observable<UserDTO> {
    console.log(`Fetching user ${id} with auth headers`);
    return this.http.get<UserDTO>(`${this.apiUrl}/${id}`, this.getAuthHeaders()).pipe(
      catchError((error) => this.handleError(error, `fetching user ${id}`))
    );
  }

  createUser(user: UserDTO): Observable<UserDTO> {
    console.log('Creating user with auth headers:', user);
    return this.http.post<UserDTO>(this.apiUrl, user, this.getAuthHeaders()).pipe(
      catchError((error) => this.handleError(error, 'creating user'))
    );
  }

  updateUser(id: number | undefined, user: UserDTO): Observable<UserDTO> {
    if (id === undefined || id === null) {
      return throwError(() => new Error('Invalid user ID: ID cannot be undefined or null'));
    }

    console.log(`Updating user ${id} with auth headers:`, user);
    return this.http.put<UserDTO>(`${this.apiUrl}/${id}`, user, this.getAuthHeaders()).pipe(
      catchError((error) => this.handleError(error, `updating user ${id}`))
    );
  }

  deleteUser(id: number): Observable<void> {
    console.log(`Deleting user ${id} with auth headers`);

    const headers = this.getAuthHeaders().headers;
    console.log('Delete request headers:', headers.keys().map(key => `${key}: ${headers.get(key)}`));

    return this.http.delete<void>(`${this.apiUrl}/${id}`, this.getAuthHeaders()).pipe(
      tap(() => console.log(`Successfully deleted user ${id}`)),
      catchError((error) => this.handleError(error, `deleting user ${id}`))
    );
  }
}
