package ma.hariti.asmaa.wrm.simulator.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.dto.request.*;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import ma.hariti.asmaa.wrm.simulator.repository.UserRepository;
import ma.hariti.asmaa.wrm.simulator.service.serviceDefault.AIInterviewServiceDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final AIInterviewServiceDefault aiInterviewService;
    private final UserRepository userRepository;
    @PostMapping("/start")
    public InterviewSessionDTO startNewSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String position,
            @RequestParam String specialization,
            @RequestParam String experienceLevel
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return aiInterviewService.startNewSession(user.getId(), position, specialization, experienceLevel);
    }

    @PostMapping("/process-answer")
    public AnswerDTO processAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long sessionId,
            @RequestParam Long questionId,
            @RequestParam String answer
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return aiInterviewService.processAnswer(userId, sessionId, questionId, answer);
    }

    @GetMapping("/next-question")
    public QuestionDTO generateNextQuestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long sessionId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return aiInterviewService.generateNextQuestion(userId, sessionId);
    }
}