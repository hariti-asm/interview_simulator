package ma.hariti.asmaa.wrm.simulator.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSessionDTO {
    private Long id;
    private String position;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Float finalScore;
    private String specialization;
    private String experienceLevel;
    private List<String> strongPoints = new ArrayList<>();
    private List<String> weakPoints = new ArrayList<>();
    private List<QuestionDTO> questions = new ArrayList<>();
    private List<String> skills = new ArrayList<>();
    private Long userId;
    private Float score;}

