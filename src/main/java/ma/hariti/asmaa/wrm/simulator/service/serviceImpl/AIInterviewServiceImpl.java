package ma.hariti.asmaa.wrm.simulator.service.serviceImpl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import ma.hariti.asmaa.wrm.simulator.mapper.AnswerMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.InterviewSessionMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.QuestionMapper;
import ma.hariti.asmaa.wrm.simulator.repository.InterviewSessionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.QuestionRepository;
import ma.hariti.asmaa.wrm.simulator.service.AIInterviewService;
import ma.hariti.asmaa.wrm.simulator.service.AnswerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInterviewServiceImpl implements AIInterviewService {
    private final AIServiceImpl aiService;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewSessionMapper sessionMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final AnswerService answerService;
    private final QuestionRepository questionRepository;

    @Transactional

    public InterviewSessionDTO startNewSession(String position, String specialization, String experienceLevel) {
        log.info("Starting new session with position: {}, specialization: {}, level: {}",
                position, specialization, experienceLevel);

        InterviewSession session = new InterviewSession();
        session.setPosition(position);
        session.setStartTime(LocalDateTime.now());
        session.setSpecialization(specialization);
        session.setExperienceLevel(experienceLevel);

        String initialContext = aiService.generateInitialContext(position, experienceLevel);
        log.info("Generated initial context: {}", initialContext);
        session.setInterviewContext(initialContext);

        try {
            InterviewSession savedSession = sessionRepository.save(session);
            log.info("Saved session with ID: {}", savedSession.getId());
            return sessionMapper.toDTO(savedSession);
        } catch (Exception e) {
            log.error("Error saving session", e);
            throw e;
        }
    }

    @Transactional
    public AnswerDTO processAnswer(Long sessionId, Long questionId, String answer) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        String expectedAnswer = question.getExpectedAnswer();

        String feedback = aiService.generateQuestionFeedback(
                question.getContent(),
                answer
        );

        String followUpQuestion = aiService.generateFollowUpQuestion(
                question.getContent(),
                answer
        );

        Float score = answerService.calculateScore(answer, expectedAnswer);
        List<String> improvementPoints = answerService.generateImprovementSuggestions(answer, expectedAnswer);

        AnswerDTO answerDTO = answerMapper.toDTO(feedback, followUpQuestion);
        answerDTO.setScore(score);
        answerDTO.setImprovementSuggestions(improvementPoints);
        answerDTO.setContent(answer);
        answerDTO.setQuestionId(questionId);

        return answerDTO;
    }

    @Transactional
    public QuestionDTO generateNextQuestion(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        AIServiceImpl.QuestionResponse questionResponse = aiService.generateQuestion(
                session.getPosition(),
                session.getExperienceLevel(),
                session.getInterviewContext()
        );

        Question question = new Question();
        question.setContent(questionResponse.getQuestion());
        question.setExpectedAnswer(questionResponse.getExpectedAnswer());
        question.setSession(session);

        Question savedQuestion = questionRepository.save(question);

        session.getQuestions().add(savedQuestion);
        sessionRepository.save(session);

        log.info("Generated and saved question with ID: {}", savedQuestion.getId());
        return questionMapper.toDTO(savedQuestion);
    }
}