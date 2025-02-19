package ma.hariti.asmaa.wrm.simulator.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Data
    public static class InterviewSessionDTO {
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
}