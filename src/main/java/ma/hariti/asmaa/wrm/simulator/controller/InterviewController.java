package ma.hariti.asmaa.wrm.simulator.controller;

import lombok.RequiredArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.dto.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.service.AIInterviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final AIInterviewService aiInterviewService;

    @PostMapping("/start")
    public InterviewSessionDTO startNewSession(
            @RequestParam String position,
            @RequestParam String specialization,
            @RequestParam String experienceLevel
    ) {


        return aiInterviewService.startNewSession(position, specialization, experienceLevel);
    }

    @PostMapping("/process-answer")
    public AnswerDTO processAnswer(
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