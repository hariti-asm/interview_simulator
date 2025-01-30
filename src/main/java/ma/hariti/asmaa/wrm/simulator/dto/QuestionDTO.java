package ma.hariti.asmaa.wrm.simulator.dto;


import lombok.Data;

@Data
public class QuestionDTO {
    private Long id;
    private String content;
    private String expectedAnswer;
    private Long sessionId;
}