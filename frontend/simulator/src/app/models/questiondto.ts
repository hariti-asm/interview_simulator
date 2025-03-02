import {AnswerDTO} from './answerdto';

export interface QuestionDTO {
  id: number;
  sessionId: number;
  content: string;
  expectedAnswer?: string;
  questionType?: string;
  difficultyLevel?: string;
  order?: number;
  answer?: AnswerDTO;
}
