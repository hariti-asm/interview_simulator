export interface InterviewSessionDTO {
  id: number;
  position: string;
  startTime: string;
  endTime: string | null;
  finalScore: number | null;
  strongPoints: string[] | null;
  weakPoints: string[] | null;
  userId: number;
  questions: any[] | null;
}
