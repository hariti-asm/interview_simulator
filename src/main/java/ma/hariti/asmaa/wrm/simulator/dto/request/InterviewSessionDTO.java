package ma.hariti.asmaa.wrm.simulator.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public  class InterviewSessionDTO {
    private Long id;
    private String position;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Float finalScore;
    private List<String> strongPoints;
    private List<String> weakPoints;
    private Long userId;
    private List<QuestionDTO> questions;
}