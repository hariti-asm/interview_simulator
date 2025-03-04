package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import ma.hariti.asmaa.wrm.simulator.entity.Question;
import ma.hariti.asmaa.wrm.simulator.service.QuestionService;

import java.util.List;

public class QuestionServiceDefault implements QuestionService {
    @Override
    public Question createQuestion(Long sessionId, String content, String expectedAnswer) {
        return null;
    }

    @Override
    public Question getQuestion(Long questionId) {
        return null;
    }

    @Override
    public List<Question> getSessionQuestions(Long sessionId) {
        return List.of();
    }

    @Override
    public Question generateNextQuestion(Long sessionId, String position) {
        return null;
    }
}
