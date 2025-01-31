package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.entity.Answer;
import org.springframework.stereotype.Service;


import java.util.UUID;
import java.util.List;

public interface AnswerService {
    Answer submitAnswer(Long questionId, String content);
    Answer evaluateAnswer(Long answerId);
    void addImprovementSuggestion(Long answerId, String suggestion);
    Float calculateScore(String userAnswer, String expectedAnswer);
    List<String> generateImprovementSuggestions(String userAnswer, String expectedAnswer);
}