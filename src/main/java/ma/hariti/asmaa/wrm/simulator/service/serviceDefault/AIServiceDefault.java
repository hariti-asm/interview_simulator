package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.response.QuestionResponse;
import ma.hariti.asmaa.wrm.simulator.service.AIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AIServiceDefault implements AIService {

    private final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public AIServiceDefault(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String generateInitialContext(String position, String experienceLevel) {
        String prompt = String.format(
                "Create an initial context for a technical interview for a %s position with %s experience level. " +
                        "Focus on relevant technical skills and experience requirements.",
                position, experienceLevel
        );
        return callOpenAI(prompt);
    }

    @Override
    public QuestionResponse generateQuestion(String position, String experienceLevel, String context) {
        String prompt = String.format(
                "Based on the context of interviewing for a %s position with %s experience level, " +
                        "and considering the previous context: '%s', generate a technical interview question, " +
                        "its expected answer, and the primary skill category this question tests. " +
                        "Format the response exactly like this:\n" +
                        "QUESTION: [your question here]\n" +
                        "EXPECTED_ANSWER: [detailed expected answer here]\n" +
                        "SKILL: [primary skill being tested, e.g. Java, Algorithms, System Design, Database, etc.]",
                position, experienceLevel, context
        );

        String response = callOpenAI(prompt);
        return parseQuestionResponse(response);
    }

    private QuestionResponse parseQuestionResponse(String response) {
        QuestionResponse qr = new QuestionResponse();

        // Check for SKILL tag first
        String[] skillParts = response.split("SKILL:");
        String skillValue = null;

        if (skillParts.length >= 2) {
            skillValue = skillParts[1].trim();
        }

        // Now parse for question and answer
        String[] parts = response.split("EXPECTED_ANSWER:");

        if (parts.length >= 2) {
            String questionPart = parts[0].replace("QUESTION:", "").trim();

            String answerPart;
            if (skillParts.length >= 2) {
                answerPart = parts[1].split("SKILL:")[0].trim();
            } else {
                answerPart = parts[1].trim();
            }

            qr.setQuestion(questionPart);
            qr.setExpectedAnswer(answerPart);
            qr.setSkill(skillValue != null ? skillValue : "General Technical");
        } else {
            throw new IllegalStateException("Invalid response format from AI");
        }

        return qr;
    }

    @Override
    public String generateQuestionFeedback(String question, String answer) {
        String prompt = String.format(
                "Give a concise feedback (under 200 characters). Evaluate the answer to this interview question: '%s'. " +
                        "The candidate's answer is: '%s'. Focus on the most critical aspects of the response.",
                question, answer
        );
        return callOpenAI(prompt);
    }

    @Override
    public String generateFollowUpQuestion(String question, String answer) {
        String prompt = String.format(
                "Based on the interview question: '%s' and the answer: '%s', generate a relevant follow-up question.",
                question, answer
        );
        return callOpenAI(prompt);
    }

    @Override
    public String generatePerformanceAnalysis(List<InterviewSessionDTO> sessions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analysis request for ").append(sessions.size()).append(" interview sessions.\n");

        for (InterviewSessionDTO session : sessions) {
            sb.append("Position: ").append(session.getPosition())
                    .append(", Score: ").append(session.getFinalScore())
                    .append("\n");
        }

        String prompt = String.format(
                "Analyze the following interview performance data and provide a comprehensive assessment:\n%s\n" +
                        "Focus on trends, strengths, weaknesses, and specific areas for improvement. " +
                        "Also suggest next steps for career development based on the performance data.",
                sb.toString()
        );

        return callOpenAI(prompt);
    }

    @Override
    public String generateSkillsAssessment(List<QuestionDTO> answeredQuestions) {
        StringBuilder sb = new StringBuilder();

        for (QuestionDTO question : answeredQuestions) {
            sb.append("Question: ").append(question.getContent())
                    .append("\nSkill: ").append(question.getSkill() != null ? question.getSkill() : "General Technical")
                    .append("\nAnswer: ").append(question.getAnswer() != null ? question.getAnswer().getContent() : "No answer")
                    .append("\nScore: ").append(question.getAnswer() != null ? question.getAnswer().getScore() : 0)
                    .append("\n\n");
        }

        String prompt = String.format(
                "Based on the following interview questions and answers, assess the technical skills demonstrated:\n%s\n" +
                        "Categorize skills by proficiency level (Beginner, Intermediate, Advanced, Expert) " +
                        "and provide specific evidence from the answers for each assessment.",
                sb.toString()
        );

        return callOpenAI(prompt);
    }

    @Override
    public String generateImprovementSuggestions(List<AnswerDTO> userAnswers) {
        StringBuilder sb = new StringBuilder();

        for (AnswerDTO answer : userAnswers) {
            sb.append("Question ID: ").append(answer.getQuestionId())
                    .append("\nAnswer: ").append(answer.getContent())
                    .append("\nScore: ").append(answer.getScore())
                    .append("\nFeedback: ").append(answer.getFeedback())
                    .append("\n\n");
        }

        String prompt = String.format(
                "Based on the following interview answers and feedback, suggest concrete improvements:\n%s\n" +
                        "Provide specific learning resources, practice exercises, and skill development strategies " +
                        "tailored to the candidate's current level. Focus on the most critical areas for improvement first.",
                sb.toString()
        );

        return callOpenAI(prompt);
    }

    @Override
    public Map<String, List<String>> analyzeAnswer(String questionContent, String userAnswer, String expectedAnswer) {
        log.info("Analyzing answer for question: {}", questionContent.substring(0, Math.min(50, questionContent.length())) + "...");

        String prompt = String.format(
                "You are an expert technical interviewer. Analyze the following interview answer:\n\n" +
                        "QUESTION: %s\n\n" +
                        "CANDIDATE'S ANSWER: %s\n\n" +
                        "EXPECTED ANSWER: %s\n\n" +
                        "Identify the strong points and weak points of the candidate's answer.\n" +
                        "Format your response exactly like this:\n" +
                        "STRONG_POINTS:\n" +
                        "- [strong point 1]\n" +
                        "- [strong point 2]\n" +
                        "...\n\n" +
                        "WEAK_POINTS:\n" +
                        "- [weak point 1]\n" +
                        "- [weak point 2]\n" +
                        "...",
                questionContent, userAnswer, expectedAnswer
        );

        String response = callOpenAI(prompt);
        log.info("Received AI analysis response of length: {}", response.length());

        Map<String, List<String>> result = parseAnalysisResponse(response);
        log.info("Parsed {} strong points and {} weak points from AI response",
                result.getOrDefault("strongPoints", Collections.emptyList()).size(),
                result.getOrDefault("weakPoints", Collections.emptyList()).size());

        return result;
    }

    private Map<String, List<String>> parseAnalysisResponse(String response) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> strongPoints = new ArrayList<>();
        List<String> weakPoints = new ArrayList<>();

        try {
            if (!response.contains("STRONG_POINTS:") || !response.contains("WEAK_POINTS:")) {
                log.warn("AI response does not contain expected sections. Response: {}",
                        response.substring(0, Math.min(200, response.length())) + "...");

                strongPoints.add("Demonstrated some understanding of the topic");
                weakPoints.add("Response could be more comprehensive");

                result.put("strongPoints", strongPoints);
                result.put("weakPoints", weakPoints);
                return result;
            }

            String[] parts = response.split("WEAK_POINTS:");
            if (parts.length >= 1) {
                String strongPointsSection = parts[0].replace("STRONG_POINTS:", "").trim();
                for (String line : strongPointsSection.split("\n")) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("-")) {
                        strongPoints.add(trimmed.substring(1).trim());
                    }
                }
            }

            if (parts.length >= 2) {
                String weakPointsSection = parts[1].trim();
                for (String line : weakPointsSection.split("\n")) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("-")) {
                        weakPoints.add(trimmed.substring(1).trim());
                    }
                }
            }

            // If we couldn't extract any points, add defaults
            if (strongPoints.isEmpty()) {
                strongPoints.add("Attempted to answer the question");
            }

            if (weakPoints.isEmpty()) {
                weakPoints.add("Could improve answer comprehensiveness");
            }
        } catch (Exception e) {
            log.error("Error parsing AI analysis response: {}", e.getMessage(), e);
            // Add default points in case of error
            strongPoints.add("Attempted to answer the question");
            weakPoints.add("Could improve answer clarity and structure");
        }

        result.put("strongPoints", strongPoints);
        result.put("weakPoints", weakPoints);
        return result;
    }

    private String callOpenAI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an experienced technical interviewer."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("Sending request to OpenAI API with prompt: {}", prompt);
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_API_URL, request, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            throw new RuntimeException("Invalid response format from OpenAI API");

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Authentication failed with OpenAI API. Please check your API key.", e);
            throw new RuntimeException("Failed to authenticate with OpenAI API. Please check your API key.", e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("Rate limit exceeded with OpenAI API.", e);
            throw new RuntimeException("OpenAI API rate limit exceeded. Please try again later.", e);
        } catch (HttpClientErrorException e) {
            log.error("HTTP error occurred while calling OpenAI API: {}", e.getStatusCode(), e);
            throw new RuntimeException("Error calling OpenAI API: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            log.error("Error occurred while calling OpenAI API", e);
            throw new RuntimeException("Failed to communicate with OpenAI API", e);
        }
    }
}

