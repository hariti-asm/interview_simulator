package ma.hariti.asmaa.wrm.simulator.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.entity.Answer;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import ma.hariti.asmaa.wrm.simulator.repository.AnswerRepository;
import ma.hariti.asmaa.wrm.simulator.repository.QuestionRepository;
import ma.hariti.asmaa.wrm.simulator.service.AnswerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private static final float CONTENT_WEIGHT = 0.5f;
    private static final float TECHNICAL_TERMS_WEIGHT = 0.3f;
    private static final float STRUCTURE_WEIGHT = 0.2f;

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

        return answerRepository.save(answer);
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

        return answerRepository.save(answer);
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

        float finalScore = (contentScore * CONTENT_WEIGHT) +
                (technicalScore * TECHNICAL_TERMS_WEIGHT) +
                (structureScore * STRUCTURE_WEIGHT);

        return Math.min(100.0f, Math.max(0.0f, finalScore));
    }

    @Override
    public List<String> generateImprovementSuggestions(String userAnswer, String expectedAnswer) {
        List<String> suggestions = new ArrayList<>();

        // Content analysis
        Set<String> expectedKeywords = extractKeywords(expectedAnswer);
        Set<String> userKeywords = extractKeywords(userAnswer);
        Set<String> missingKeywords = new HashSet<>(expectedKeywords);
        missingKeywords.removeAll(userKeywords);

        if (!missingKeywords.isEmpty()) {
            suggestions.add("Include these key concepts: " + String.join(", ", missingKeywords));
        }

        // Length analysis
        int expectedLength = expectedAnswer.split("\\s+").length;
        int userLength = userAnswer.split("\\s+").length;
        if (userLength < expectedLength * 0.7) {
            suggestions.add("Your answer needs more detail. Consider expanding your explanation.");
        } else if (userLength > expectedLength * 1.5) {
            suggestions.add("Try to be more concise while keeping the key points.");
        }

        // Technical terms analysis
        Set<String> expectedTechnicalTerms = extractTechnicalTerms(expectedAnswer);
        Set<String> userTechnicalTerms = extractTechnicalTerms(userAnswer);
        Set<String> missingTerms = new HashSet<>(expectedTechnicalTerms);
        missingTerms.removeAll(userTechnicalTerms);

        if (!missingTerms.isEmpty()) {
            suggestions.add("Consider using these technical terms: " + String.join(", ", missingTerms));
        }

        if (!hasProperStructure(userAnswer)) {
            suggestions.add("Structure your answer better with an introduction, main points, and conclusion.");
        }

        return suggestions;
    }

    private float calculateContentScore(String userAnswer, String expectedAnswer) {
        Set<String> expectedKeywords = extractKeywords(expectedAnswer);
        Set<String> userKeywords = extractKeywords(userAnswer);

        if (expectedKeywords.isEmpty()) {
            return 0.0f;
        }

        int matchingKeywords = 0;
        for (String keyword : userKeywords) {
            if (expectedKeywords.contains(keyword)) {
                matchingKeywords++;
            }
        }

        return (float) matchingKeywords / expectedKeywords.size() * 100;
    }

    private float calculateTechnicalTermsScore(String userAnswer, String expectedAnswer) {
        Set<String> expectedTerms = extractTechnicalTerms(expectedAnswer);
        Set<String> userTerms = extractTechnicalTerms(userAnswer);

        if (expectedTerms.isEmpty()) {
            return 0.0f;
        }

        int matchingTerms = 0;
        for (String term : userTerms) {
            if (expectedTerms.contains(term)) {
                matchingTerms++;
            }
        }

        return (float) matchingTerms / expectedTerms.size() * 100;
    }

    private float calculateStructureScore(String answer) {
        float score = 0;

        if (Pattern.compile("(?i)(first|initially|to begin with|introduction)").matcher(answer).find()) {
            score += 30;
        }

        if (Pattern.compile("(?i)(secondly|furthermore|moreover|in addition|next)").matcher(answer).find()) {
            score += 40;
        }

        if (Pattern.compile("(?i)(finally|in conclusion|to summarize|therefore|thus)").matcher(answer).find()) {
            score += 30;
        }

        return score;
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
                "|testing|security|authentication|cache|performance|scalability)");

        Arrays.stream(text.split("\\s+"))
                .filter(word -> technicalPattern.matcher(word).find())
                .forEach(technicalTerms::add);

        return technicalTerms;
    }

    private boolean hasProperStructure(String text) {
        int structureElements = 0;

        // Check for introduction
        if (Pattern.compile("(?i)(first|initially|to begin with|introduction)").matcher(text).find()) {
            structureElements++;
        }

        // Check for main points
        if (Pattern.compile("(?i)(secondly|furthermore|moreover|in addition|next)").matcher(text).find()) {
            structureElements++;
        }

        // Check for conclusion
        if (Pattern.compile("(?i)(finally|in conclusion|to summarize|therefore|thus)").matcher(text).find()) {
            structureElements++;
        }

        return structureElements >= 2;
    }
}
