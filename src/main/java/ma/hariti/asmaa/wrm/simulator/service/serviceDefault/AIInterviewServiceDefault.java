package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.response.PerformanceData;
import ma.hariti.asmaa.wrm.simulator.dto.response.QuestionResponse;
import ma.hariti.asmaa.wrm.simulator.entity.*;
import ma.hariti.asmaa.wrm.simulator.mapper.AnswerMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.InterviewSessionMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.QuestionMapper;
import ma.hariti.asmaa.wrm.simulator.repository.*;
import ma.hariti.asmaa.wrm.simulator.service.AIInterviewService;
import ma.hariti.asmaa.wrm.simulator.service.AnswerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final SkillRepository skillRepository;
private final InterviewSkillRepository interviewSkillRepository;
    @Transactional
    public InterviewSessionDTO startNewSession(Long userId, String position, String specialization, String experienceLevel) {
        InterviewSession session = new InterviewSession();

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            session.setUser(user);
            log.info("Starting new session for user {} with position: {}, specialization: {}, level: {}",
                    userId, position, specialization, experienceLevel);
        } else {
            log.info("Starting new anonymous session with position: {}, specialization: {}, level: {}",
                    position, specialization, experienceLevel);
        }

        session.setPosition(position);
        session.setStartTime(LocalDateTime.now());
        session.setSpecialization(specialization);
        session.setExperienceLevel(experienceLevel);
        session.setInterviewContext(aiService.generateInitialContext(position, experienceLevel));

        // First save the session to get an ID
        InterviewSession savedSession = sessionRepository.save(session);
        log.info("Saved new interview session with ID: {}", savedSession.getId());

        // Now create and save interview skills separately
        List<Skill> relevantSkills = skillRepository.findByRelevantPositionsContaining(position);
        log.info("Found {} relevant skills for position: {}", relevantSkills.size(), position);

        // If no skills found with the exact position, try to find some default skills
        if (relevantSkills.isEmpty()) {
            log.warn("No skills found for position '{}', trying to find default skills", position);
            relevantSkills = skillRepository.findAll();

            // Filter skills that might be relevant (optional)
            if (!relevantSkills.isEmpty()) {
                log.info("Found {} total skills to use as fallback", relevantSkills.size());
                // Optionally limit to a reasonable number if there are too many
                if (relevantSkills.size() > 5) {
                    relevantSkills = relevantSkills.subList(0, 5);
                }
            } else {
                log.warn("No skills found in the database at all");
                // Create at least one default skill if none exist
                Skill defaultSkill = new Skill();
                defaultSkill.setName("General " + position + " Skills");
                defaultSkill.setCategory("Technical");
                defaultSkill.setIsActive(true);
                defaultSkill = skillRepository.save(defaultSkill);
                relevantSkills = Collections.singletonList(defaultSkill);
                log.info("Created default skill: {}", defaultSkill.getName());
            }
        }

        List<InterviewSkill> interviewSkills = new ArrayList<>();

        for (Skill skill : relevantSkills) {
            InterviewSkill interviewSkill = new InterviewSkill();
            interviewSkill.setInterview(savedSession);
            interviewSkill.setSkill(skill);
            interviewSkills.add(interviewSkill);
            log.debug("Creating interview skill association: session={}, skill={}",
                    savedSession.getId(), skill.getName());
        }

        // Save all interview skills using a separate repository
        if (!interviewSkills.isEmpty()) {
            interviewSkillRepository.saveAll(interviewSkills);
            log.info("Saved {} interview skills for session {}", interviewSkills.size(), savedSession.getId());
        }

        // Refresh the session to ensure it has the latest data
        savedSession = sessionRepository.findById(savedSession.getId()).orElse(savedSession);

        return sessionMapper.toDTO(savedSession);
    }
    @Transactional
    public AnswerDTO processAnswer(Long userId, Long sessionId, Long questionId, String answer) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        // First, check if there's an existing answer and delete it if it exists
        Optional<Answer> existingAnswer = answerRepository.findByQuestionId(questionId);
        if (existingAnswer.isPresent()) {
            // Either update the existing answer or delete it first
            answerRepository.delete(existingAnswer.get());
        }

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

        // Create a new answer
        Answer answerEntity = new Answer();
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
        log.info("Getting performance by skill for user: {}", userId);

        // Get all user's interview sessions
        List<InterviewSession> userSessions = sessionRepository.findByUserId(userId);

        if (userSessions.isEmpty()) {
            log.info("No interview sessions found for user: {}", userId);
            return Collections.emptyList();
        }

        // Get all skills used in these interviews
        Set<Long> sessionIds = userSessions.stream()
                .map(InterviewSession::getId)
                .collect(Collectors.toSet());

        // Find all interview skills for these sessions
        List<InterviewSkill> interviewSkills = interviewSkillRepository.findByInterviewIdIn(sessionIds);

        if (interviewSkills.isEmpty()) {
            log.info("No skills found for user's interviews: {}", userId);
            return Collections.emptyList();
        }

        // Group by skill
        Map<Skill, List<InterviewSkill>> skillMap = interviewSkills.stream()
                .collect(Collectors.groupingBy(InterviewSkill::getSkill));

        // Create performance data for each skill
        List<PerformanceData> result = new ArrayList<>();

        for (Map.Entry<Skill, List<InterviewSkill>> entry : skillMap.entrySet()) {
            Skill skill = entry.getKey();
            List<InterviewSkill> skillOccurrences = entry.getValue();

            PerformanceData performanceData = new PerformanceData();
            performanceData.setSkill(skill.getName());
            performanceData.setSkill(skill.getName());
            performanceData.setQuestionCount(skillOccurrences.size());

            // Calculate average score for questions related to this skill
            double totalScore = 0.0;
            int answeredQuestions = 0;

            for (InterviewSkill interviewSkill : skillOccurrences) {
                InterviewSession session = interviewSkill.getInterview();

                // Calculate session score
                if (session.getQuestions() != null && !session.getQuestions().isEmpty()) {
                    for (Question question : session.getQuestions()) {
                        // Check if the question has an answer with a score
                        if (question.getAnswer() != null && question.getAnswer().getScore() != null) {
                            totalScore += question.getAnswer().getScore();
                            answeredQuestions++;
                        }
                    }
                }
            }

            double averageScore = answeredQuestions > 0 ? totalScore / answeredQuestions : 0.0;
            performanceData.setScore(averageScore);

            result.add(performanceData);
        }

        log.info("Found {} skills with performance data for user {}", result.size(), userId);
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