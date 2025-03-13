import { Injectable } from '@angular/core';
import {SkillService} from './skill.service';

@Injectable({
  providedIn: 'root'
})
export class SkillSeeder {
  constructor(private skillService: SkillService) {}

  seedSkills(): void {
    const skills = [
      {
        name: "JavaScript",
        category: "Programming Languages",
        description: "High-level, interpreted programming language that is one of the core technologies of the World Wide Web.",
        skillType: "Technical",
        isActive: true
      },
    ];

    skills.forEach(skill => {
      this.skillService.createSkill(skill).subscribe({
        next: (createdSkill) => console.log(`Created skill: ${createdSkill.name}`),
        error: (error) => console.error(`Error creating skill ${skill.name}:`, error)
      });
    });
  }
}
