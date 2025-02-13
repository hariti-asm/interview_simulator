package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.service.serviceDefault.AIServiceDefault;

public interface AIService {
    String generateInitialContext(String position, String experienceLevel);

    AIServiceDefault.QuestionResponse generateQuestion(String position, String experienceLevel, String context);

    String generateQuestionFeedback(String question, String answer);

    String generateFollowUpQuestion(String question, String answer);
}
