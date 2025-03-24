package ma.hariti.asmaa.wrm.simulator.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.response.PerformanceData;
import ma.hariti.asmaa.wrm.simulator.repository.InterviewSessionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.UserRepository;
import ma.hariti.asmaa.wrm.simulator.security.UserDetailsImpl;
import ma.hariti.asmaa.wrm.simulator.service.AIInterviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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


    private Long extractAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("No authentication found in security context");
            throw new IllegalStateException("User must be authenticated");
        }

        log.debug("Authentication principal type: {}",
                authentication.getPrincipal().getClass().getName());

        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            if (userId == null) {
                log.error("UserDetailsImpl found but ID is null");
                throw new IllegalStateException("User ID not found in authentication");
            }

            log.debug("Extracted user ID: {} from UserDetailsImpl", userId);
            return userId;
        }

        log.error("Unsupported principal type: {}",
                authentication.getPrincipal().getClass().getName());
        throw new IllegalStateException("Unsupported authentication type");
    }

    @PostMapping("/start")
    public ResponseEntity<?> startNewSession(
            @RequestParam String position,
            @RequestParam String specialization,
            @RequestParam String experienceLevel
    ) {
        log.info("Starting new interview session with position={}, specialization={}, experienceLevel={}",
                position, specialization, experienceLevel);

        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Starting interview for authenticated user ID: {}", userId);

            InterviewSessionDTO session = aiInterviewService.startNewSession(
                    userId, position, specialization, experienceLevel);

            log.info("Created new interview session with id: {}", session.getId());
            return ResponseEntity.ok(session);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (Exception e) {
            log.error("Error starting interview session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start interview session: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @PostMapping("/process-answer")
    public ResponseEntity<?> processAnswer(
            @RequestParam Long sessionId,
            @RequestParam Long questionId,
            @RequestParam String answer
    ) {
        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Processing answer for session {} question {} by user {}",
                    sessionId, questionId, userId);

            AnswerDTO result = aiInterviewService.processAnswer(userId, sessionId, questionId, answer);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 404));
        } catch (Exception e) {
            log.error("Error processing answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process answer: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @PostMapping("/sessions/{sessionId}/questions/{questionId}/answers")
    public ResponseEntity<?> submitAnswer(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody AnswerDTO answerDTO
    ) {
        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Submitting answer for session {} question {} by user {}",
                    sessionId, questionId, userId);

            AnswerDTO result = aiInterviewService.processAnswer(
                    userId, sessionId, questionId, answerDTO.getContent());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 404));
        } catch (Exception e) {
            log.error("Error submitting answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit answer: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping("/next-question")
    public ResponseEntity<?> generateNextQuestion(@RequestParam Long sessionId) {
        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Generating next question for session {} user {}", sessionId, userId);

            QuestionDTO question = aiInterviewService.generateNextQuestion(userId, sessionId);
            return ResponseEntity.ok(question);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 404));
        } catch (Exception e) {
            log.error("Error generating next question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate question: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> deleteInterview(@PathVariable Long sessionId) {
        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Deleting interview session {} for user {}", sessionId, userId);

            aiInterviewService.deleteInterview(userId, sessionId);
            return ResponseEntity.ok(Map.of("message", "Interview deleted successfully",
                    "success", true, "status", 200));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 404));
        } catch (Exception e) {
            log.error("Error deleting interview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete interview: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getInterviewById(@PathVariable Long sessionId) {
        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Fetching interview session {} for user {}", sessionId, userId);

            InterviewSessionDTO session = aiInterviewService.getInterviewById(userId, sessionId);
            return ResponseEntity.ok(session);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 404));
        } catch (Exception e) {
            log.error("Error fetching interview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch interview: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping("/session/{sessionId}/questions")
    public ResponseEntity<?> getSessionQuestions(@PathVariable Long sessionId) {
        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Fetching questions for session {} user {}", sessionId, userId);

            List<QuestionDTO> questions = aiInterviewService.getQuestionsBySessionId(userId, sessionId);
            return ResponseEntity.ok(questions);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 404));
        } catch (Exception e) {
            log.error("Error fetching session questions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch questions: " + e.getMessage(),
                            "success", false, "status", 500));
        }
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

    @GetMapping("/performance/skills")
    public ResponseEntity<?> getPerformanceBySkill() {
        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Fetching performance by skill for user {}", userId);

            List<PerformanceData> performance = aiInterviewService.getPerformanceBySkill(userId);
            return ResponseEntity.ok(performance);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (Exception e) {
            log.error("Error fetching performance data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch performance data: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping("/session/{sessionId}/feedback")
    public ResponseEntity<?> getInterviewFeedback(@PathVariable Long sessionId) {
        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Fetching feedback for session {} user {}", sessionId, userId);

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
            feedback.put("success", true);
            feedback.put("status", 200);

            return ResponseEntity.ok(feedback);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 404));
        } catch (Exception e) {
            log.error("Error fetching interview feedback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch feedback: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping("/performance/summary")
    public ResponseEntity<?> getOverallPerformanceData() {
        try {
            Long userId = extractAuthenticatedUserId();
            log.info("Fetching performance summary for user {}", userId);

            Map<String, Object> performance = aiInterviewService.getOverallPerformance(userId);
            performance.put("success", true);
            performance.put("status", 200);
            return ResponseEntity.ok(performance);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (Exception e) {
            log.error("Error fetching performance summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch performance summary: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping("/performance/skills/{userId}")
    public ResponseEntity<?> getUserSkillPerformance(@PathVariable Long userId) {
        log.info("Fetching performance by skill for specified user {}", userId);
        try {
            List<PerformanceData> performance = aiInterviewService.getPerformanceBySkill(userId);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            log.error("Error fetching performance data for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch performance data: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }
}

