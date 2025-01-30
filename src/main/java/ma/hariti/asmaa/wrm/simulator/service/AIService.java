package ma.hariti.asmaa.wrm.simulator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public String generateInitialContext(String position, String experienceLevel) {
        String prompt = String.format(
                "Create an initial context for a technical interview for a %s position with %s experience level. " +
                        "Focus on relevant technical skills and experience requirements.",
                position, experienceLevel
        );
        return callOpenAI(prompt);
    }

    public String generateQuestion(String position, String experienceLevel, String context) {
        String prompt = String.format(
                "Based on the context of interviewing for a %s position with %s experience level, " +
                        "and considering the previous context: '%s', generate a relevant technical interview question.",
                position, experienceLevel, context
        );
        return callOpenAI(prompt);
    }

    public String generateQuestionFeedback(String question, String answer) {
        String prompt = String.format(
                "Given the interview question: '%s' and the answer: '%s', provide detailed feedback.",
                question, answer
        );
        return callOpenAI(prompt);
    }

    public String generateFollowUpQuestion(String question, String answer) {
        String prompt = String.format(
                "Based on the interview question: '%s' and the answer: '%s', generate a relevant follow-up question.",
                question, answer
        );
        return callOpenAI(prompt);
    }

    private String callOpenAI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

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

        ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_API_URL, request, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }

        return "No response from OpenAI.";
    }
}
