export interface AnswerDTO {
  id: number;
  sessionId: number;
  questionId: number;
  answerText: string;
  feedback?: string;
  score?: number;
}
