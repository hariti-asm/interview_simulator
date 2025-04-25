import {inject, Injectable} from "@angular/core"
import {HttpClient, type HttpErrorResponse, HttpHeaders} from "@angular/common/http"
import {type Observable, throwError} from "rxjs"
import {catchError, tap} from "rxjs/operators"
import type {UserDTO} from "../models/userdto"
import {AuthService} from "./auth.service"

@Injectable({
  providedIn: "root",
})
export class AdminService {
  private apiUrl = "http://localhost:8083/api/users"

  private http = inject(HttpClient)
  private authService = inject(AuthService)

  getAllUsers(): Observable<UserDTO[]> {
    console.log("Fetching all users with auth headers")
    const options = this.getAuthHeaders()
    console.log(
      "Request headers:",
      options.headers.keys().map((key) => `${key}: ${options.headers.get(key)}`),
    )

    return this.http
      .get<UserDTO[]>(this.apiUrl, options)
      .pipe(catchError((error) => this.handleError(error, "fetching all users")))
  }

  getUserById(id: number): Observable<UserDTO> {
    console.log(`Fetching user ${id} with auth headers`)
    return this.http
      .get<UserDTO>(`${this.apiUrl}/${id}`, this.getAuthHeaders())
      .pipe(catchError((error) => this.handleError(error, `fetching user ${id}`)))
  }

  createUser(user: UserDTO): Observable<UserDTO> {
    console.log("Creating user with auth headers:", user)
    const options = this.getAuthHeaders()
    console.log(
      "Request headers:",
      options.headers.keys().map((key) => `${key}: ${options.headers.get(key)}`),
    )

    return this.http
      .post<UserDTO>(this.apiUrl, user, options)
      .pipe(catchError((error) => this.handleError(error, "creating user")))
  }

  updateUser(id: number | undefined, user: UserDTO): Observable<UserDTO> {
    if (id === undefined || id === null) {
      return throwError(() => new Error("Invalid user ID: ID cannot be undefined or null"))
    }

    console.log(`Updating user ${id} with auth headers:`, user)
    return this.http
      .put<UserDTO>(`${this.apiUrl}/${id}`, user, this.getAuthHeaders())
      .pipe(catchError((error) => this.handleError(error, `updating user ${id}`)))
  }

  deleteUser(id: number): Observable<void> {
    console.log(`Deleting user ${id} with auth headers`)

    const options = this.getAuthHeaders()
    console.log(
      "Delete request headers:",
      options.headers.keys().map((key) => `${key}: ${options.headers.get(key)}`),
    )

    return this.http.delete<void>(`${this.apiUrl}/${id}`, options).pipe(
      tap(() => console.log(`Successfully deleted user ${id}`)),
      catchError((error) => this.handleError(error, `deleting user ${id}`)),
    )
  }

  private getAuthHeaders() {
    const token = this.authService.getToken()

    console.log("Using token:", token ? `${token.substring(0, 15)}...` : "No token found")

    if (!token) {
      console.error("No token available for request")
    }

    return {
      headers: new HttpHeaders({
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      }),
    }
  }

  private handleError(error: HttpErrorResponse, operation: string) {
    console.error(`Error during ${operation}:`, error)

    if (error.status === 403) {
      console.error("Authorization error: You may not have permission for this action")

      if (error.headers) {
        console.error(
          "Response headers:",
          error.headers.keys().map((key) => `${key}: ${error.headers.get(key)}`),
        )
      }

      const token = this.authService.getToken()
      if (token) {
        try {
          const tokenParts = token.split(".")
          if (tokenParts.length === 3) {
            const tokenData = JSON.parse(atob(tokenParts[1]))
            console.log("Decoded token payload:", tokenData)

            const expiry = tokenData.exp * 1000
            const now = Date.now()

            if (expiry < now) {
              console.error("Token is expired. Expiry:", new Date(expiry), "Now:", new Date(now))
            } else {
              const role =
                tokenData.role ||
                (Array.isArray(tokenData.roles) && tokenData.roles.length > 0 ? tokenData.roles[0] : null) ||
                (Array.isArray(tokenData.authorities) && tokenData.authorities.length > 0
                  ? tokenData.authorities[0]
                  : null)

              console.log("Token not expired. Role information:", role || "No role found in token")

              if (!role) {
                console.error("No role information in token. This is likely the cause of the 403 error.")
                console.log("Full token payload for debugging:", tokenData)
              }
            }
          } else {
            console.error("Invalid token format")
          }
        } catch (e) {
          console.error("Error parsing token:", e)
        }
      }
    }

    return throwError(() => error)
  }
}

