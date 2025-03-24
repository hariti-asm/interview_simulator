package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.entity.Answer;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.repository.AnswerRepository;
import ma.hariti.asmaa.wrm.simulator.repository.InterviewSessionRepository;
import ma.hariti.asmaa.wrm.simulator.service.AIAnalysisService;
import ma.hariti.asmaa.wrm.simulator.service.AIService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisServiceDefault implements AIAnalysisService {

    private final AIService aiService;
    private final InterviewSessionRepository sessionRepository;
    private final AnswerRepository answerRepository;

    @Override
    public Map<String, List<String>> analyzeAnswer(String questionContent, String userAnswer, String expectedAnswer) {
        log.info("Analyzing answer for question: {}",
                questionContent.substring(0, Math.min(50, questionContent.length())) + "...");

        return aiService.analyzeAnswer(questionContent, userAnswer, expectedAnswer);
    }

    @Override
    @Transactional
    public boolean updateSessionPoints(Long sessionId, Long answerId) {
        try {
            InterviewSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));

            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new EntityNotFoundException("Answer not found with id: " + answerId));

            String userAnswer = answer.getContent();
            String expectedAnswer = answer.getQuestion().getExpectedAnswer();
            String questionContent = answer.getQuestion().getContent();

            Map<String, List<String>> analysis = aiService.analyzeAnswer(
                    questionContent, userAnswer, expectedAnswer);

            if (session.getStrongPoints() == null) {
                session.setStrongPoints(new ArrayList<>());
            }

            if (session.getWeakPoints() == null) {
                session.setWeakPoints(new ArrayList<>());
            }

            // Add new points to the session (avoiding duplicates)
            if (analysis.containsKey("strongPoints")) {
                for (String point : analysis.get("strongPoints")) {
                    if (!session.getStrongPoints().contains(point)) {
                        session.getStrongPoints().add(point);
                    }
                }
            }

            if (analysis.containsKey("weakPoints")) {
                for (String point : analysis.get("weakPoints")) {
                    if (!session.getWeakPoints().contains(point)) {
                        session.getWeakPoints().add(point);
                    }
                }
            }

            sessionRepository.save(session);

            log.info("Updated session {} with {} strong points and {} weak points",
                    sessionId,
                    session.getStrongPoints().size(),
                    session.getWeakPoints().size());

            return true;
        } catch (Exception e) {
            log.error("Error updating session points: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public void addStrongPoint(Long sessionId, String point) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));

        if (session.getStrongPoints() == null) {
            session.setStrongPoints(new ArrayList<>());
        }

        if (!session.getStrongPoints().contains(point)) {
            session.getStrongPoints().add(point);
            sessionRepository.save(session);
            log.info("Added strong point to session {}: {}", sessionId, point);
        }
    }

    @Override
    @Transactional
    public void addWeakPoint(Long sessionId, String point) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));

        if (session.getWeakPoints() == null) {
            session.setWeakPoints(new ArrayList<>());
        }

        if (!session.getWeakPoints().contains(point)) {
            session.getWeakPoints().add(point);
            sessionRepository.save(session);
            log.info("Added weak point to session {}: {}", sessionId, point);
        }
    }
}

