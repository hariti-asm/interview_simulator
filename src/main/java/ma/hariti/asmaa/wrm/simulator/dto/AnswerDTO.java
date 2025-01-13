package ma.hariti.asmaa.wrm.simulator.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class AnswerDTO {
    private UUID id;
    private String content;
    private Float score;
    private List<String> improvementSuggestions;
    private UUID questionId;
}
