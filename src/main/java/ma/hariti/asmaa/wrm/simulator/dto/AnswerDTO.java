package ma.hariti.asmaa.wrm.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {
    private Long id;
    private String content;
    private Float score;
    private List<String> improvementSuggestions;
    private Long questionId;
    private String feedback;
    private String followUpQuestion;
}