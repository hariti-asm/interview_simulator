package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.entity.Answer;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import ma.hariti.asmaa.wrm.simulator.repository.AnswerRepository;
import ma.hariti.asmaa.wrm.simulator.repository.InterviewSessionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.QuestionRepository;
import ma.hariti.asmaa.wrm.simulator.service.AIService;
import ma.hariti.asmaa.wrm.simulator.service.AnswerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerServiceDefault implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final AIService aiService;

    private static final float CONTENT_WEIGHT = 0.9f;
    private static final float TECHNICAL_TERMS_WEIGHT = 0.5f;
    private static final float STRUCTURE_WEIGHT = 0.3f;
    private static final int MAX_SUGGESTION_LENGTH = 500;

    @Override
    @Transactional
    public Answer submitAnswer(Long questionId, String content) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setContent(content);

        float score = calculateScore(content, question.getExpectedAnswer());
        List<String> suggestions = generateImprovementSuggestions(content, question.getExpectedAnswer());

        answer.setScore(score);
        answer.setImprovementSuggestions(suggestions);

        Answer savedAnswer = answerRepository.save(answer);

        // Update session with weak and strong points
        updateSessionWeakAndStrongPoints(question.getSession().getId(), savedAnswer.getId());

        return savedAnswer;
    }

    @Override
    @Transactional
    public Answer evaluateAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found with id: " + answerId));

        String userAnswer = answer.getContent();
        String expectedAnswer = answer.getQuestion().getExpectedAnswer();

        float score = calculateScore(userAnswer, expectedAnswer);
        List<String> suggestions = generateImprovementSuggestions(userAnswer, expectedAnswer);

        answer.setScore(score);
        answer.setImprovementSuggestions(suggestions);

        Answer savedAnswer = answerRepository.save(answer);

        // Update session with weak and strong points
        updateSessionWeakAndStrongPoints(answer.getQuestion().getSession().getId(), savedAnswer.getId());

        return savedAnswer;
    }

    @Override
    @Transactional
    public void addImprovementSuggestion(Long answerId, String suggestion) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found with id: " + answerId));

        List<String> suggestions = answer.getImprovementSuggestions();
        suggestions.add(suggestion);

        answerRepository.save(answer);
    }

    @Override
    public Float calculateScore(String userAnswer, String expectedAnswer) {
        if (userAnswer == null || expectedAnswer == null) {
            return 0.0f;
        }

        float contentScore = calculateContentScore(userAnswer, expectedAnswer);
        float technicalScore = calculateTechnicalTermsScore(userAnswer, expectedAnswer);
        float structureScore = calculateStructureScore(userAnswer);

        float baseScore = 60.0f;

        float additionalScore = (contentScore * CONTENT_WEIGHT) +
                (technicalScore * TECHNICAL_TERMS_WEIGHT) +
                (structureScore * STRUCTURE_WEIGHT);

        float finalScore = baseScore + (additionalScore * 0.4f);

        return Math.min(100.0f, Math.max(0.0f, finalScore));
    }

    private float calculateContentScore(String userAnswer, String expectedAnswer) {
        Set<String> expectedKeywords = extractKeywords(expectedAnswer);
        Set<String> userKeywords = extractKeywords(userAnswer);

        if (expectedKeywords.isEmpty()) {
            return 70.0f;
        }

        Set<String> commonWords = new HashSet<>(Arrays.asList(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "with", "by", "is", "are", "was", "were"
        ));
        expectedKeywords.removeAll(commonWords);
        userKeywords.removeAll(commonWords);

        if (expectedKeywords.isEmpty()) {
            return 70.0f;
        }

        int matchingKeywords = 0;
        for (String keyword : userKeywords) {
            if (expectedKeywords.contains(keyword)) {
                matchingKeywords++;
            }
        }

        float coverage = (float) matchingKeywords / expectedKeywords.size();
        float bonusForAdditionalKeywords = Math.min(0.2f, (float)(userKeywords.size() - matchingKeywords) / 20);

        return Math.min(100.0f, (coverage * 85.0f) + (bonusForAdditionalKeywords * 100.0f));
    }

    private float calculateTechnicalTermsScore(String userAnswer, String expectedAnswer) {
        Set<String> expectedTerms = extractTechnicalTerms(expectedAnswer);
        Set<String> userTerms = extractTechnicalTerms(userAnswer);

        if (expectedTerms.isEmpty()) {
            return 70.0f;
        }

        int matchingTerms = 0;
        for (String term : userTerms) {
            if (expectedTerms.contains(term)) {
                matchingTerms++;
            }
        }

        float coverage = expectedTerms.isEmpty() ? 1.0f : (float) matchingTerms / expectedTerms.size();
        float bonusForAdditionalTerms = Math.min(0.3f, (float)(userTerms.size() - matchingTerms) / 10);

        return Math.min(100.0f, (coverage * 80.0f) + (bonusForAdditionalTerms * 100.0f));
    }

    private float calculateStructureScore(String answer) {
        float score = 60.0f;

        if (Pattern.compile("(?i)(first|initially|to begin with|introduction)").matcher(answer).find()) {
            score += 10;
        }

        if (Pattern.compile("(?i)(secondly|furthermore|moreover|in addition|next)").matcher(answer).find()) {
            score += 15;
        }

        if (Pattern.compile("(?i)(finally|in conclusion|to summarize|therefore|thus)").matcher(answer).find()) {
            score += 15;
        }

        int wordCount = answer.split("\\s+").length;
        if (wordCount >= 100) {
            score += 10;
        } else if (wordCount >= 50) {
            score += 5;
        }

        return Math.min(100.0f, score);
    }

    private Set<String> extractKeywords(String text) {
        return new HashSet<>(Arrays.asList(text.toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", " ")
                .split("\\s+")));
    }

    private Set<String> extractTechnicalTerms(String text) {
        Set<String> technicalTerms = new HashSet<>();
        Pattern technicalPattern = Pattern.compile("(?i)(api|rest|soap|mvc|spring|hibernate|jpa|sql|database" +
                "|algorithm|data structure|design pattern|microservice|docker|kubernetes|git" +
                "|testing|security|authentication|cache|performance|scalability" +
                "|java|python|javascript|html|css|node|react|angular|vue|aws|azure|cloud" +
                "|function|method|class|object|inheritance|polymorphism|encapsulation|abstraction" +
                "|thread|concurrency|parallelism|singleton|factory|observer|strategy|dependency" +
                "|injection|framework|library|component|module|service|repository|controller)");

        Arrays.stream(text.split("\\s+"))
                .filter(word -> technicalPattern.matcher(word).find())
                .forEach(technicalTerms::add);

        return technicalTerms;
    }

    private String truncate(String suggestion) {
        return suggestion.length() > MAX_SUGGESTION_LENGTH
                ? suggestion.substring(0, MAX_SUGGESTION_LENGTH)
                : suggestion;
    }

    @Override
    public List<String> generateImprovementSuggestions(String userAnswer, String expectedAnswer) {
        List<String> suggestions = new ArrayList<>();

        Set<String> expectedKeywords = extractKeywords(expectedAnswer);
        Set<String> userKeywords = extractKeywords(userAnswer);
        Set<String> missingKeywords = new HashSet<>(expectedKeywords);
        missingKeywords.removeAll(userKeywords);

        if (!missingKeywords.isEmpty()) {
            suggestions.add(truncate("Add missing key concepts to improve your answer's depth and comprehensiveness."));
        }

        int expectedLength = expectedAnswer.split("\\s+").length;
        int userLength = userAnswer.split("\\s+").length;
        if (userLength < expectedLength * 0.7) {
            suggestions.add(truncate("Provide more detailed explanations and include specific examples to support your key points."));
        } else if (userLength > expectedLength * 1.5) {
            suggestions.add(truncate("Condense your answer. Focus on the most critical points and remove unnecessary elaboration."));
        }

        Set<String> expectedTechnicalTerms = extractTechnicalTerms(expectedAnswer);
        Set<String> userTechnicalTerms = extractTechnicalTerms(userAnswer);
        Set<String> missingTerms = new HashSet<>(expectedTechnicalTerms);
        missingTerms.removeAll(userTechnicalTerms);

        if (!missingTerms.isEmpty()) {
            suggestions.add(truncate("Incorporate more precise technical terminology to demonstrate professional knowledge and understanding."));
        }

        if (!hasProperStructure(userAnswer)) {
            suggestions.add(truncate("Improve answer structure. Use clear sections with introduction, main points, and conclusion."));
        }

        if (userAnswer.toLowerCase().contains("like") || userAnswer.toLowerCase().contains("um")) {
            suggestions.add(truncate("Enhance communication clarity. Avoid filler words and speak with confidence."));
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Your answer shows solid understanding.");
        }

        return suggestions.stream()
                .limit(4)
                .collect(Collectors.toList());
    }

    private boolean hasProperStructure(String text) {
        int structureElements = 0;

        if (Pattern.compile("(?i)(first|initially|to begin with|introduction)").matcher(text).find()) {
            structureElements++;
        }

        if (Pattern.compile("(?i)(secondly|furthermore|moreover|in addition|next)").matcher(text).find()) {
            structureElements++;
        }

        if (Pattern.compile("(?i)(finally|in conclusion|to summarize|therefore|thus)").matcher(text).find()) {
            structureElements++;
        }

        return structureElements >= 2;
    }

    @Override
    @Transactional
    public void updateSessionWeakAndStrongPoints(Long sessionId, Long answerId) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found with id: " + answerId));

        String userAnswer = answer.getContent();
        String expectedAnswer = answer.getQuestion().getExpectedAnswer();
        String questionContent = answer.getQuestion().getContent();

        Map<String, List<String>> analysis = aiService.analyzeAnswer(
                questionContent, userAnswer, expectedAnswer);

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

        interviewSessionRepository.save(session);
    }
}

