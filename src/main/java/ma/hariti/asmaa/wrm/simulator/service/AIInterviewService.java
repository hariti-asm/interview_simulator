package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.dto.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.QuestionDTO;

public interface AIInterviewService {
    InterviewSessionDTO startNewSession(String position, String specialization, String experienceLevel);

    AnswerDTO processAnswer(Long sessionId, Long questionId, String answer);

    QuestionDTO generateNextQuestion(Long sessionId);
}
