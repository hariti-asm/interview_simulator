package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.response.PerformanceData;
import ma.hariti.asmaa.wrm.simulator.dto.response.QuestionResponse;
import ma.hariti.asmaa.wrm.simulator.entity.Answer;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import ma.hariti.asmaa.wrm.simulator.mapper.AnswerMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.InterviewSessionMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.QuestionMapper;
import ma.hariti.asmaa.wrm.simulator.repository.AnswerRepository;
import ma.hariti.asmaa.wrm.simulator.repository.InterviewSessionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.QuestionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.UserRepository;
import ma.hariti.asmaa.wrm.simulator.service.AIInterviewService;
import ma.hariti.asmaa.wrm.simulator.service.AnswerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    private final AnswerRepository answerRepository;

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

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        Optional<Answer> existingAnswer = question.getAnswer() != null
                ? Optional.of(question.getAnswer())
                : Optional.empty();

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

        Answer answerEntity;
        answerEntity = existingAnswer.orElseGet(Answer::new);

        answerEntity.setContent(answer);
        answerEntity.setQuestion(question);
        answerEntity.setScore(score);
        answerEntity.setImprovementSuggestions(improvementPoints);

        Answer savedAnswer = answerRepository.save(answerEntity);

        question.setAnswer(savedAnswer);
        questionRepository.save(question);

        AnswerDTO answerDTO = answerMapper.toDTO(savedAnswer);
        answerDTO.setContent(answer);
        answerDTO.setQuestionId(questionId);
        answerDTO.setFeedback(feedback);
        answerDTO.setFollowUpQuestion(followUpQuestion);

        return answerDTO;
    }
    @Transactional
    public QuestionDTO generateNextQuestion(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

     QuestionResponse questionResponse = aiService.generateQuestion(
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

        log.info("Generated and saved question with ID: {} for user: {}, skill: {}",
                savedQuestion.getId(), userId, questionResponse.getSkill());

        QuestionDTO questionDTO = questionMapper.toDTO(savedQuestion);
        return questionDTO;
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

    @Override
    public InterviewSessionDTO getInterviewById(Long userId, Long sessionId) {
        log.info("Fetching interview session {} for user {}", sessionId, userId);

        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Interview session not found for user " + userId));

        return sessionMapper.toDTO(session);
    }

    @Transactional
    public List<QuestionDTO> getQuestionsBySessionId(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found for user " + userId));

        List<Question> questions = session.getQuestions();

        return questions.stream()
                .map(questionMapper::toDTO)
                .toList();
    }
    @Override
    public List<PerformanceData> getPerformanceBySkill(Long userId) {
        // You need to implement getUserInterviews method or get data directly from repository
        List<InterviewSession> userSessions = sessionRepository.findByUserId(userId);
        List<InterviewSessionDTO> userSessionDTOs = userSessions.stream()
                .map(sessionMapper::toDTO)
                .toList();

        Map<String, List<Double>> skillScores = new HashMap<>();

        for (InterviewSessionDTO session : userSessionDTOs) {
            if (session.getQuestions() != null) {
                for (QuestionDTO question : session.getQuestions()) {
                    if (question.getSkill() != null && question.getAnswer() != null) {
                        skillScores.computeIfAbsent(question.getSkill(), k -> new ArrayList<>())
                                .add((double) question.getAnswer().getScore());
                    }
                }
            }
        }

        List<PerformanceData> result = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : skillScores.entrySet()) {
            double averageScore = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            PerformanceData data = new PerformanceData();
            data.setSkill(entry.getKey());
            data.setScore(averageScore);
            data.setQuestionCount(entry.getValue().size());
            result.add(data);
        }

        return result;
    }

    @Override
    public Map<String, Object> getOverallPerformance(Long userId) {
        List<InterviewSession> userSessions = sessionRepository.findByUserId(userId);
        List<InterviewSessionDTO> userSessionDTOs = userSessions.stream()
                .map(sessionMapper::toDTO)
                .toList();

        double overallScore = userSessionDTOs.stream()
                .map(InterviewSessionDTO::getFinalScore)
                .filter(Objects::nonNull)
                .mapToDouble(Float::doubleValue)
                .average()
                .orElse(0.0);

        int totalInterviews = userSessionDTOs.size();
        int totalQuestions = userSessionDTOs.stream()
                .mapToInt(s -> s.getQuestions() != null ? s.getQuestions().size() : 0)
                .sum();

        String performanceAnalysis = aiService.generatePerformanceAnalysis(userSessionDTOs);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("overallScore", overallScore);
        result.put("totalInterviews", totalInterviews);
        result.put("totalQuestions", totalQuestions);
        result.put("analysis", performanceAnalysis);

        return result;
    }
    @Override
    public Map<String, Object> getOverallPerformanceData(Long userId) {
        log.info("Fetching overall performance data for user {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            List<InterviewSession> userSessions = sessionRepository.findByUserId(userId);

            int totalInterviews = userSessions.size();
            int totalQuestions = 0;
            double totalScore = 0.0;
            int answeredQuestions = 0;

            Map<String, Integer> positionCounts = new HashMap<>();
            Map<String, Double> positionScores = new HashMap<>();

            for (InterviewSession session : userSessions) {
                String position = session.getPosition();
                positionCounts.put(position, positionCounts.getOrDefault(position, 0) + 1);

                if (session.getQuestions() != null) {
                    totalQuestions += session.getQuestions().size();

                    for (Question question : session.getQuestions()) {
                        if (question.getAnswer() != null && question.getAnswer().getScore() != null) {
                            double score = question.getAnswer().getScore();
                            totalScore += score;
                            answeredQuestions++;

                            positionScores.put(position,
                                    positionScores.getOrDefault(position, 0.0) + score);
                        }
                    }
                }
            }

            double overallScore = answeredQuestions > 0 ? totalScore / answeredQuestions : 0.0;

            Map<String, Double> positionAverages = new HashMap<>();
            for (String position : positionCounts.keySet()) {
                int count = positionCounts.get(position);
                double total = positionScores.getOrDefault(position, 0.0);
                positionAverages.put(position, count > 0 ? total / count : 0.0);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("username", user.getName());
            result.put("overallScore", overallScore);
            result.put("totalInterviews", totalInterviews);
            result.put("totalQuestions", totalQuestions);
            result.put("answeredQuestions", answeredQuestions);
            result.put("positionStats", positionAverages);
            result.put("recentSessionCount", Math.min(totalInterviews, 5));

            return result;
        } catch (Exception e) {
            log.error("Error getting overall performance data for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "An unexpected error occurred");
            errorResult.put("userId", userId);
            return errorResult;
        }
    }
    private double calculateSessionScore(InterviewSession session) {
        if (session.getQuestions() == null || session.getQuestions().isEmpty()) {
            return 0.0;
        }

        double totalScore = 0.0;
        int answeredQuestions = 0;

        for (Question question : session.getQuestions()) {
            if (question.getAnswer() != null && question.getAnswer().getScore() != null) {
                totalScore += question.getAnswer().getScore();
                answeredQuestions++;
            }
        }

        return answeredQuestions > 0 ? totalScore / answeredQuestions : 0.0;
    }


    private Double calculateImprovementRate(List<InterviewSession> sessions) {
        if (sessions.size() < 2) {
            return null;
        }

        List<InterviewSession> sortedSessions = new ArrayList<>(sessions);
        sortedSessions.sort(Comparator.comparing(InterviewSession::getStartTime));

        double firstSessionScore = calculateSessionScore(sortedSessions.get(0));
        double lastSessionScore = calculateSessionScore(sortedSessions.get(sortedSessions.size() - 1));

        if (firstSessionScore > 0) {
            return ((lastSessionScore - firstSessionScore) / firstSessionScore) * 100;
        } else {
            return lastSessionScore > 0 ? 100.0 : 0.0;
        }
    }
}