package ma.hariti.asmaa.wrm.simulator.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnswerDTO {
    private Long id;
    private String content;
    private Float score;
    private List<String> improvementSuggestions;
    private Long questionId;
}
