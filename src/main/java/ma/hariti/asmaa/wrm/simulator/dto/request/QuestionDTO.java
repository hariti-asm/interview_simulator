package ma.hariti.asmaa.wrm.simulator.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String content;
    private String expectedAnswer;
    private Long sessionId;
    private String skill;
    private AnswerDTO answer;
}