package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.request.*;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import ma.hariti.asmaa.wrm.simulator.mapper.AnswerMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.InterviewSessionMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.QuestionMapper;
import ma.hariti.asmaa.wrm.simulator.repository.InterviewSessionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.QuestionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.UserRepository;
import ma.hariti.asmaa.wrm.simulator.service.AIInterviewService;
import ma.hariti.asmaa.wrm.simulator.service.AnswerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInterviewServiceDefault implements AIInterviewService {
    private final AIServiceDefault aiService;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewSessionMapper sessionMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final AnswerService answerService;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public InterviewSessionDTO startNewSession(Long userId, String position, String specialization, String experienceLevel) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        log.info("Starting new session for user {} with position: {}, specialization: {}, level: {}",
                userId, position, specialization, experienceLevel);

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setPosition(position);
        session.setStartTime(LocalDateTime.now());
        session.setSpecialization(specialization);
        session.setExperienceLevel(experienceLevel);
        session.setInterviewContext(aiService.generateInitialContext(position, experienceLevel));

        try {
            InterviewSession savedSession = sessionRepository.save(session);
            log.info("Saved session with ID: {} for user: {}", savedSession.getId(), userId);
            return sessionMapper.toDTO(savedSession);
        } catch (Exception e) {
            log.error("Error saving session for user: {}", userId, e);
            throw e;
        }
    }
    @Transactional
    public AnswerDTO processAnswer(Long userId, Long sessionId, Long questionId, String answer) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));

//        if (!userId.equals(session.getUser().getId())) {
//            throw new SecurityException("User not authorized to access this session");
//        }

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
    public QuestionDTO generateNextQuestion(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));


        AIServiceDefault.QuestionResponse questionResponse = aiService.generateQuestion(
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

        log.info("Generated and saved question with ID: {} for user: {}", savedQuestion.getId(), userId);
        return questionMapper.toDTO(savedQuestion);
    }

    @Override
    @Transactional
    public void deleteInterview(Long userId, Long sessionId) {
        log.info("Deleting interview session {} for user {}", sessionId, userId);

        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Interview session not found"));

        sessionRepository.delete(session);

        log.info("Interview session {} deleted successfully", sessionId);
    }
}