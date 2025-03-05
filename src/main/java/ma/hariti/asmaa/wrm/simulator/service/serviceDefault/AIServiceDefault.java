package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class AIServiceDefault implements AIService {

    private final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    @Value("${openai.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate;
    public AIServiceDefault(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .build();
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
                        "and considering the previous context: '%s', generate a technical interview question " +
                        "AND its expected answer. Format the response exactly like this:\n" +
                        "QUESTION: [your question here]\n" +
                        "EXPECTED_ANSWER: [detailed expected answer here]",
                position, experienceLevel, context
        );

        String response = callOpenAI(prompt);
        return parseQuestionResponse(response);
    }

    private QuestionResponse parseQuestionResponse(String response) {
        QuestionResponse qr = new QuestionResponse();
        String[] parts = response.split("EXPECTED_ANSWER:");

        if (parts.length >= 2) {
            String questionPart = parts[0].replace("QUESTION:", "").trim();
            String answerPart = parts[1].trim();

            qr.setQuestion(questionPart);
            qr.setExpectedAnswer(answerPart);
        } else {
            throw new IllegalStateException("Invalid response format from AI");
        }

        return qr;
    }

    @Override
    public String generateQuestionFeedback(String question, String answer) {
        String prompt = String.format(
                "Give a concise feedback (under 200 characters). Evaluate the answer to this interview question: '%s'. Focus on the most critical aspects of the response.",
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
    @Data
    public static class QuestionResponse {
        private String question;
        private String expectedAnswer;
    }
}
