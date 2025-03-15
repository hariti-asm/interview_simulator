import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';

export interface SkillDTO {
  id?: number;
  name: string;
  description?: string;
  category: string;
  skillType?: string;
  proficiencyLevels?: string[];
  keywords?: string[];
  relevantPositions?: string[];
  weight?: number;
  isActive?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SkillService {
  private apiUrl = 'http://localhost:8083/api/skill';

  constructor(private http: HttpClient) {}

  createSkill(skill: SkillDTO): Observable<SkillDTO> {
    return this.http.post<SkillDTO>(this.apiUrl, skill);
  }

  getSkillById(id: number): Observable<SkillDTO> {
    return this.http.get<SkillDTO>(`${this.apiUrl}/${id}`);
  }

  getAllSkills(): Observable<SkillDTO[]> {
    return this.http.get<SkillDTO[]>(this.apiUrl);
  }
  updateSkill(id: number | undefined, skill: SkillDTO): Observable<SkillDTO> {
    if (id === undefined || id === null) {
      return throwError(() => new Error('Invalid skill ID: ID cannot be undefined or null'));
    }

    return this.http.put<SkillDTO>(`${this.apiUrl}/${id}`, skill);
  }

  deleteSkill(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
