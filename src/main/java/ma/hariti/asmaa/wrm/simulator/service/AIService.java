package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.response.QuestionResponse;
import ma.hariti.asmaa.wrm.simulator.service.serviceDefault.AIServiceDefault;

import java.util.List;

public interface AIService {
    String generateInitialContext(String position, String experienceLevel);

  QuestionResponse generateQuestion(String position, String experienceLevel, String context);

    String generateQuestionFeedback(String question, String answer);

    String generateFollowUpQuestion(String question, String answer);
    String generatePerformanceAnalysis(List<InterviewSessionDTO> sessions);
    String generateSkillsAssessment(List<QuestionDTO> answeredQuestions);

    String generateImprovementSuggestions(List<AnswerDTO> userAnswers);

}
