package ma.hariti.asmaa.wrm.simulator.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.response.PerformanceData;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import ma.hariti.asmaa.wrm.simulator.repository.InterviewSessionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.UserRepository;
import ma.hariti.asmaa.wrm.simulator.security.UserDetailsImpl;
import ma.hariti.asmaa.wrm.simulator.service.AIInterviewService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Slf4j
public class InterviewController {

    private final AIInterviewService aiInterviewService;
    private final UserRepository userRepository;
private final InterviewSessionRepository interviewSessionRepository;
    @PostMapping("/start")
    public InterviewSessionDTO startNewSession(
            @RequestParam String position,
            @RequestParam String specialization,
            @RequestParam String experienceLevel
    ) {
        log.info("Starting new interview session with position={}, specialization={}, experienceLevel={}",
                position, specialization, experienceLevel);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.error("Authentication is null");
            throw new IllegalStateException("Authentication required");
        }

        log.info("Authentication name: {}", authentication.getName());
        log.info("Authentication principal type: {}",
                authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null");

        Long userId = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            userId = ((UserDetailsImpl) principal).getId();
            log.info("User ID from UserDetailsImpl: {}", userId);
        } else {
            log.error("Unsupported authentication principal type: {}",
                    principal != null ? principal.getClass().getName() : "null");
            throw new IllegalStateException("Unsupported authentication principal type");
        }

        if (userId == null) {
            log.error("User ID is null");
            throw new IllegalStateException("User ID is null");
        }

        log.info("Found user ID: {}", userId);

        InterviewSessionDTO session = aiInterviewService.startNewSession(
                userId, position, specialization, experienceLevel);

        log.info("Created new interview session with id: {}", session.getId());

        return session;
    }

    @PostMapping("/process-answer")
    public AnswerDTO processAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long sessionId,
            @RequestParam Long questionId,
            @RequestParam String answer
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return aiInterviewService.processAnswer(user.getId(), sessionId, questionId, answer);
    }
    @PostMapping("/sessions/{sessionId}/questions/{questionId}/answers")
    public AnswerDTO submitAnswer(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody AnswerDTO answerDTO,
            @RequestParam(required = false) Long userId // Temporary solution
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = null;

        // Try to get user ID from authentication
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
            log.info("User ID from authentication: {}", authenticatedUserId);
        }
        // Use provided userId parameter as fallback (TEMPORARY, NOT SECURE)
        else if (userId != null) {
            authenticatedUserId = userId;
            log.warn("Using userId from request parameter: {}. This is insecure and should be temporary!", userId);
        }
        else {
            log.error("Authentication failed and no userId provided");
            throw new IllegalStateException("Authentication required");
        }

        return aiInterviewService.processAnswer(authenticatedUserId, sessionId, questionId, answerDTO.getContent());
    }

    @GetMapping("/next-question")
    public QuestionDTO generateNextQuestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long sessionId
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return aiInterviewService.generateNextQuestion(user.getId(), sessionId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public void deleteInterview(@PathVariable Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.error("Authentication is null");
            throw new IllegalStateException("Authentication required");
        }

        log.info("Authentication name: {}", authentication.getName());
        log.info("Authentication principal type: {}",
                authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null");

        Long userId = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            userId = ((UserDetailsImpl) principal).getId();
            log.info("User ID from UserDetailsImpl: {}", userId);
        } else {
            log.error("Unsupported authentication principal type: {}",
                    principal != null ? principal.getClass().getName() : "null");
            throw new IllegalStateException("Unsupported authentication principal type");
        }

        if (userId == null) {
            log.error("User ID is null");
            throw new IllegalStateException("User ID is null");
        }

        log.info("Deleting interview session {} for user {}", sessionId, userId);

        aiInterviewService.deleteInterview(userId, sessionId);

        log.info("Interview session {} deleted successfully", sessionId);
    }

    @GetMapping("/{sessionId}")
    public InterviewSessionDTO getInterviewById(@PathVariable Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("Authentication is missing or invalid");
            throw new IllegalStateException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        Long userId;

        if (principal instanceof UserDetailsImpl) {
            userId = ((UserDetailsImpl) principal).getId();
            log.info("Fetching interview session {} for authenticated user {}", sessionId, userId);
        } else {
            log.error("Unsupported authentication principal type: {}", principal.getClass().getName());
            throw new IllegalStateException("Unsupported authentication principal type");
        }

        return aiInterviewService.getInterviewById(userId, sessionId);
    }

    @GetMapping("/session/{sessionId}/questions")
    public List<QuestionDTO> getSessionQuestions(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long sessionId) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return aiInterviewService.getQuestionsBySessionId(user.getId(), sessionId);
    }
    @GetMapping("/positions/count")
    public List<Map<String, Object>> getInterviewPositionCounts() {
        List<Object[]> results = interviewSessionRepository.countInterviewsByPosition();

        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("position", result[0]);
            map.put("count", result[1]);
            return map;
        }).collect(Collectors.toList());
    }
    @GetMapping("/performance/skills/{userId}")
    public List<PerformanceData> getUserSkillPerformance(@PathVariable Long userId) {
        log.info("Fetching performance by skill for specified user {}", userId);
        return aiInterviewService.getPerformanceBySkill(userId);
    }
    @GetMapping("/performance/skills")
    public List<PerformanceData> getPerformanceBySkill() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            log.error("Authentication is missing or invalid");
            throw new IllegalStateException("Authentication required");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        log.info("Fetching performance by skill for user {}", userId);
        return aiInterviewService.getPerformanceBySkill(userId);
    }
    @GetMapping("/session/{sessionId}/feedback")
    public Map<String, Object> getInterviewFeedback(@PathVariable Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            log.error("Authentication is missing or invalid");
            throw new IllegalStateException("Authentication required");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        InterviewSessionDTO session = aiInterviewService.getInterviewById(userId, sessionId);

        // Calculate average score from questions
        double totalScore = 0;
        int answeredQuestions = 0;

        if (session.getQuestions() != null) {
            for (QuestionDTO question : session.getQuestions()) {
                if (question.getAnswer() != null && question.getAnswer().getScore() != null) {
                    totalScore += question.getAnswer().getScore();
                    answeredQuestions++;
                }
            }
        }

        double averageScore = answeredQuestions > 0 ? totalScore / answeredQuestions : 0;

        Map<String, Object> feedback = new HashMap<>();
        feedback.put("sessionId", sessionId);
        feedback.put("position", session.getPosition());
        feedback.put("specialization", session.getSpecialization());
        feedback.put("experienceLevel", session.getExperienceLevel());
        feedback.put("score", averageScore);
        feedback.put("answeredQuestions", answeredQuestions);
        feedback.put("totalQuestions", session.getQuestions() != null ? session.getQuestions().size() : 0);


        return feedback;
    }
    @GetMapping("/performance/summary")
    public Map<String, Object> getOverallPerformanceData(@RequestParam Long userId) {
        return aiInterviewService.getOverallPerformance(userId);
    }
}