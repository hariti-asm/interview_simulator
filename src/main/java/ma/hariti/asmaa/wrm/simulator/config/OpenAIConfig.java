package ma.hariti.asmaa.wrm.simulator.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @PostConstruct
    public void validateConfig() {
        log.info("OpenAI API Key present: {}", apiKey != null && !apiKey.isEmpty());
    }
}
