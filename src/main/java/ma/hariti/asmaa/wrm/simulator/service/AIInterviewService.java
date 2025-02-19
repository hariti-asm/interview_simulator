package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.dto.request.ForgotPasswordRequest;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.RegisterUserRequest;

public interface AIInterviewService {
   InterviewSessionDTO startNewSession(String position, String specialization, String experienceLevel);

    RegisterUserRequest.AnswerDTO processAnswer(Long sessionId, Long questionId, String answer);

    QuestionDTO generateNextQuestion(Long sessionId);
}
