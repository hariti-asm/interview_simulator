export interface AnswerDTO {
  id?: number;
  content: string;
  score?: number;
  feedback?: string;
  improvementSuggestions?: string[];
  questionId?: number;
  followUpQuestion?: string;
}
