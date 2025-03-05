export interface QuestionDTO {
  id: number;
  content: string;
  expectedAnswer?: string;
  sessionId: number;
  answer?: {
    content?: string;
    score?: number;
    feedback?: string;
    improvementSuggestions?: string[];
    followUpQuestion?: string;
  } | null;
}
