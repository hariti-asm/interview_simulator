package ma.hariti.asmaa.wrm.simulator.dto;


import lombok.Data;
import java.util.UUID;

@Data
public class QuestionDTO {
    private UUID id;
    private String content;
    private String expectedAnswer;
    private UUID sessionId;
}