export interface InterviewSessionDTO {
  id: number;
  position: string;
  specialization: string;
  experienceLevel: string;
  startTime: Date;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';
  score?: number;
}
