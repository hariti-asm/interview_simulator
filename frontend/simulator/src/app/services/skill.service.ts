import { Injectable } from "@angular/core"
import {  HttpClient, HttpHeaders } from "@angular/common/http"
import { Observable, throwError } from "rxjs"
import { AuthService } from "./auth.service"
import { catchError } from "rxjs/operators"

export interface SkillDTO {
  id?: number
  name: string
  description?: string
  category: string
  skillType?: string
  proficiencyLevels?: string[]
  keywords?: string[]
  relevantPositions?: string[]
  weight?: number
  isActive?: boolean
}

@Injectable({
  providedIn: "root",
})
export class SkillService {
  private apiUrl = "http://localhost:8083/api/skill"

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  /**
   * Get authentication headers for API requests
   * @returns HTTP options with auth headers
   */
  private getAuthHeaders() {
    const token = this.authService.getToken()
    return {
      headers: new HttpHeaders({
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      }),
    }
  }

  createSkill(skill: SkillDTO): Observable<SkillDTO> {
    console.log("Creating skill with auth headers:", skill)
    return this.http.post<SkillDTO>(this.apiUrl, skill, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error("Error creating skill:", error)
        return throwError(() => error)
      }),
    )
  }

  getSkillById(id: number): Observable<SkillDTO> {
    console.log(`Fetching skill ${id} with auth headers`)
    return this.http.get<SkillDTO>(`${this.apiUrl}/${id}`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error fetching skill ${id}:`, error)
        return throwError(() => error)
      }),
    )
  }

  getAllSkills(): Observable<SkillDTO[]> {
    console.log("Fetching all skills with auth headers")
    return this.http.get<SkillDTO[]>(this.apiUrl, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error("Error fetching all skills:", error)
        return throwError(() => error)
      }),
    )
  }

  updateSkill(id: number | undefined, skill: SkillDTO): Observable<SkillDTO> {
    if (id === undefined || id === null) {
      return throwError(() => new Error("Invalid skill ID: ID cannot be undefined or null"))
    }

    console.log(`Updating skill ${id} with auth headers:`, skill)
    return this.http.put<SkillDTO>(`${this.apiUrl}/${id}`, skill, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error updating skill ${id}:`, error)
        return throwError(() => error)
      }),
    )
  }

  deleteSkill(id: number): Observable<void> {
    console.log(`Deleting skill ${id} with auth headers`)
    return this.http.delete<void>(`${this.apiUrl}/${id}`, this.getAuthHeaders()).pipe(
      catchError((error) => {
        console.error(`Error deleting skill ${id}:`, error)
        return throwError(() => error)
      }),
    )
  }
}

