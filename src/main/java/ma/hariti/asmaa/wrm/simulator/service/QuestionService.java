package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.entity.Question;
import org.springframework.stereotype.Service;

import java.util.List;

public interface QuestionService {
    Question createQuestion(Long sessionId, String content, String expectedAnswer);
    Question getQuestion(Long questionId);
    List<Question> getSessionQuestions(Long sessionId);
    Question generateNextQuestion(Long sessionId, String position);
}