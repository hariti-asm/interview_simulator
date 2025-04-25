import {Injectable} from "@angular/core"
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router"
import {Observable, of} from "rxjs"
import {catchError, map, take} from "rxjs/operators"
import {AuthService} from "../../services/auth.service"

@Injectable({
  providedIn: "root",
})
export class RoleGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router,
  ) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ): Observable<boolean> | Promise<boolean> | boolean {
    if (!this.authService.isAuthenticated()) {
      console.log("User not authenticated, redirecting to login")
      this.router.navigate(["/login"], {queryParams: {returnUrl: state.url}})
      return false
    }

    const requiredRole = route.data["role"] as string
    console.log(`RoleGuard: Checking for required role: ${requiredRole}`)

    // Check token directly first
    const token = this.authService.getToken()
    if (token) {
      try {
        const tokenParts = token.split(".")
        if (tokenParts.length === 3) {
          const tokenData = JSON.parse(atob(tokenParts[1]))
          console.log("Token payload in RoleGuard:", tokenData)

          const userRole =
            tokenData.role ||
            (Array.isArray(tokenData.roles) && tokenData.roles.length > 0 ? tokenData.roles[0] : null) ||
            (Array.isArray(tokenData.authorities) && tokenData.authorities.length > 0 ? tokenData.authorities[0] : null)

          console.log(`Role from token: ${userRole}`)

          if (userRole === requiredRole) {
            console.log("Role matched from token payload")
            return true
          }
        }
      } catch (e) {
        console.error("Error parsing token in RoleGuard:", e)
      }
    }

    // Fallback to profile check if token check fails
    return this.authService.getUserProfile().pipe(
      take(1),
      map((profile) => {
        console.log("User profile received in RoleGuard:", profile)
        const userRole = profile?.role
        console.log(`User role from profile: ${userRole}, Required role: ${requiredRole}`)

        if (userRole === requiredRole) {
          return true
        } else {
          console.log(`Access denied: User role ${userRole} does not match required role ${requiredRole}`)
          this.router.navigate(["/dashboard"])
          return false
        }
      }),
      catchError((error) => {
        console.error("Error getting user profile in RoleGuard:", error)
        this.router.navigate(["/dashboard"])
        return of(false)
      }),
    )
  }
}

