package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.response.QuestionResponse;

import java.util.List;
import java.util.Map;

public interface AIService {
    String generateInitialContext(String position, String experienceLevel);
    QuestionResponse generateQuestion(String position, String experienceLevel, String context);
    String generateQuestionFeedback(String question, String answer);
    String generateFollowUpQuestion(String question, String answer);
    String generatePerformanceAnalysis(List<InterviewSessionDTO> sessions);
    String generateSkillsAssessment(List<QuestionDTO> answeredQuestions);
   Map<String, List<String>> analyzeAnswer(String questionContent, String userAnswer, String expectedAnswer);
    String generateImprovementSuggestions(List<AnswerDTO> userAnswers);

}
