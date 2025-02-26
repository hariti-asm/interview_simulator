
export interface InterviewSessionDTO {
  id: number;
  userId: number;
  position: string;
  specialization: string;
  experienceLevel: string;
  startTime: Date;
  endTime?: Date;
  status: string;
  overallScore?: number;
}
