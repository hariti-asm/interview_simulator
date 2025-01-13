package ma.hariti.asmaa.wrm.simulator.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class InterviewSessionDTO {
    private UUID id;
    private String position;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Float finalScore;
    private List<String> strongPoints;
    private List<String> weakPoints;
    private UUID userId;
    private List<QuestionDTO> questions;
}
