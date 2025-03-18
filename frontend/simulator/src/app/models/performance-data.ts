export interface SkillScore {
  sessionId: number
  date: Date
  score: number
}

export interface PerformanceData {
  skill: string
  skillName: string
  score: number
  questionCount: number
  scores: SkillScore[]
}
