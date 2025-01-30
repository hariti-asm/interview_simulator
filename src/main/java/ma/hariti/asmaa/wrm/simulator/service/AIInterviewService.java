package ma.hariti.asmaa.wrm.simulator.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.dto.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import ma.hariti.asmaa.wrm.simulator.mapper.AnswerMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.InterviewSessionMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.QuestionMapper;
import ma.hariti.asmaa.wrm.simulator.repository.InterviewSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AIInterviewService {
    private final AIService aiService;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewSessionMapper sessionMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;

    public InterviewSessionDTO startNewSession(String position, String specialization, String experienceLevel) {
        InterviewSession session = new InterviewSession();
        session.setPosition(position);
        session.setStartTime(LocalDateTime.now());
        session.setSpecialization(specialization);
        session.setExperienceLevel(experienceLevel);

        String initialContext = aiService.generateInitialContext(position, experienceLevel);
        session.setInterviewContext(initialContext);

        InterviewSession savedSession = sessionRepository.save(session);
        return sessionMapper.toDTO(savedSession);
    }

    public AnswerDTO processAnswer(Long sessionId, Long questionId, String answer) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));

        String feedback = aiService.generateQuestionFeedback(
                session.getInterviewContext(),
                answer
        );

        String followUpQuestion = aiService.generateFollowUpQuestion(
                session.getInterviewContext(),
                answer
        );

        return answerMapper.toDTO(feedback, followUpQuestion);
    }

    public QuestionDTO generateNextQuestion(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        String nextQuestion = aiService.generateQuestion(
                session.getPosition(),
                session.getExperienceLevel(),
                session.getInterviewContext()
        );

        Question question = new Question();
        question.setContent(nextQuestion);
        question.setSession(session);

        session.getQuestions().add(question);
        sessionRepository.save(session);

        return questionMapper.toDTO(question);
    }
}