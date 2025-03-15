import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {InterviewSessionDTO} from '../../models/interview-sessiondto';
import {QuestionDTO} from '../../models/questiondto';
import {AnswerDTO} from '../../models/answerdto';
import {InterviewService} from '../../services/interview.service';
import {HeaderComponent} from '../header/header.component';

@Component({
  selector: 'app-interview-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent, FormsModule],
  templateUrl: './interview-detail.component.html',
  styleUrls: ['./interview-detail.component.css']
})
export class InterviewDetailComponent implements OnInit {
  sessionId!: number;
  interview: InterviewSessionDTO | null = null;
  questions: QuestionDTO[] = [];
  loading = true;
  error = false;
  currentQuestionIndex = 0;
  currentAnswer: string = '';
  Math = Math;

  constructor(
    private route: ActivatedRoute,
    private interviewService: InterviewService
  ) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.sessionId = +params['id'];
      this.loadInterviewDetails();
    });
  }

  loadInterviewDetails(): void {
    this.loading = true;
    this.error = false;

    this.interviewService.getSessionDetails(this.sessionId).subscribe({
      next: (interview) => {
        this.interview = interview;
        this.loadInterviewQuestions();
      },
      error: (err) => {
        console.error('Error fetching interview details:', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  loadInterviewQuestions(): void {
    this.interviewService.getSessionQuestions(this.sessionId).subscribe({
      next: (questions) => {
        console.log('Raw Questions:', questions);

        this.questions = questions.map(question => ({
          ...question,
          answer: question.answer || null
        }));

        console.log('Processed Questions:', this.questions);

        this.loading = false;
        this.setCurrentQuestionToFirstUnanswered();
      },
      error: (err) => {
        console.error('Error fetching interview questions:', err);
        this.error = true;
        this.loading = false;
      }
    });
  }
  generateQuestion() {
    if (!this.sessionId) return;

    const alreadyAskedQuestions = this.questions.map(q => q.content);

    this.interviewService.getNextQuestion(this.sessionId, {
      excludedQuestions: alreadyAskedQuestions
    }).subscribe({
      next: (newQuestion) => {
        if (!this.questions.some(q => q.content === newQuestion.content)) {
          this.questions.push(newQuestion);
          if (this.getAnsweredCount() === this.questions.length - 1) {
            this.currentQuestionIndex = this.questions.length - 1;
          }
        } else {
          console.warn('Duplicate question generated');
        }
      },
      error: (err) => {
        console.error('Error generating question:', err);
      },
    });
  }

  setCurrentQuestionToFirstUnanswered(): void {
    for (let i = 0; i < this.questions.length; i++) {
      if (!this.questions[i].answer) {
        this.currentQuestionIndex = i;
        return;
      }
    }
    if (this.questions.length > 0) {
      this.currentQuestionIndex = this.questions.length - 1;
    }
  }

  goToNextQuestion(): void {
    if (this.currentQuestionIndex < this.questions.length - 1) {
      this.currentQuestionIndex++;
      this.currentAnswer = '';
    }
  }

  goToPreviousQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
      this.currentAnswer = '';
    }
  }
  submitAnswer(): void {
    if (!this.currentAnswer || !this.sessionId) return;

    const questionId = this.questions[this.currentQuestionIndex].id;

    const answerData: AnswerDTO = {
      content: this.currentAnswer,
      questionId: questionId
    };

    this.interviewService.submitAnswer(this.sessionId, questionId, answerData).subscribe({
      next: (answer) => {
        console.log('Submitted Answer Response:', answer);

        this.questions[this.currentQuestionIndex].answer = {
          content: answer.content,
          score: answer.score ?? 0,
          feedback: answer.feedback || '',
          improvementSuggestions: answer.improvementSuggestions ?? [],
          followUpQuestion: answer.followUpQuestion ?? ''
        };
console.log("feedback",this.questions.map(q=>q.answer?.feedback));
        this.questions = [...this.questions];

        console.log('Updated Question:', this.questions[this.currentQuestionIndex]);

        this.currentAnswer = '';
        this.moveToNextUnansweredQuestion();
      },
      error: (err) => {
        console.error('Error submitting answer:', err);
      }
    });
  }
  moveToNextUnansweredQuestion(): void {
    for (let i = this.currentQuestionIndex + 1; i < this.questions.length; i++) {
      if (!this.questions[i].answer) {
        this.currentQuestionIndex = i;
        return;
      }
    }

    for (let i = 0; i < this.currentQuestionIndex; i++) {
      if (!this.questions[i].answer) {
        this.currentQuestionIndex = i;
        return;
      }
    }


    if (this.getAnsweredCount() === this.questions.length && this.questions.length < 6) {
      this.generateQuestion();
    }
  }

  getAnsweredCount(): number {
    return this.questions.filter(q => q.answer).length;
  }
}
