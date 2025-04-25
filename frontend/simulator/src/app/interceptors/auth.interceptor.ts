import { Injectable } from "@angular/core"
import type { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from "@angular/common/http"
import { type Observable, throwError } from "rxjs"
import { catchError } from "rxjs/operators"
import type { AuthService } from "../services/auth.service"
import type { Router } from "@angular/router"

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (
      request.url.includes("/api/v1/auth/login") ||
      request.url.includes("/api/v1/auth/register") ||
      request.url.includes("/api/v1/auth/forgot-password") ||
      request.url.includes("/api/v1/auth/reset-password")
    ) {
      return next.handle(request)
    }

    const token = this.authService.getToken()

    if (token) {
      const authReq = request.clone({
        headers: request.headers.set("Authorization", `Bearer ${token}`),
      })

      console.log("Adding auth token to request:", request.url)

      return next.handle(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 401) {
            console.log("401 Unauthorized error - redirecting to login")
            this.authService.clearAuthData()
            this.router.navigate(["/login"])
          }
          return throwError(() => error)
        }),
      )
    } else {
      console.warn("No token available for request:", request.url)

      if (request.url.includes("/api/interview/")) {
        console.log("Attempting to access protected endpoint without token, redirecting to login")
        this.router.navigate(["/login"])
        return throwError(() => new Error("Authentication required"))
      }

      return next.handle(request)
    }
  }
}

