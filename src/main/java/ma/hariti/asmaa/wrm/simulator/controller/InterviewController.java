package ma.hariti.asmaa.wrm.simulator.controller;

import lombok.RequiredArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.dto.request.*;
import ma.hariti.asmaa.wrm.simulator.service.serviceDefault.AIInterviewServiceDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final AIInterviewServiceDefault aiInterviewService;

    @PostMapping("/start")
    public InterviewSessionDTO startNewSession(
            @RequestParam String position,
            @RequestParam String specialization,
            @RequestParam String experienceLevel
    ) {


        return aiInterviewService.startNewSession(position, specialization, experienceLevel);
    }

    @PostMapping("/process-answer")
    public RegisterUserRequest.AnswerDTO processAnswer(
            @RequestParam Long sessionId,
            @RequestParam Long questionId,
            @RequestParam String answer
    ) {
        return aiInterviewService.processAnswer(sessionId, questionId, answer);
    }

    @GetMapping("/next-question")
    public QuestionDTO generateNextQuestion(@RequestParam Long sessionId) {
        return aiInterviewService.generateNextQuestion(sessionId);
    }
}