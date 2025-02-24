export interface PerformanceData {
  skillName: string;
  scores: {
    sessionId: number;
    date: Date;
    score: number;
  }[];
}
