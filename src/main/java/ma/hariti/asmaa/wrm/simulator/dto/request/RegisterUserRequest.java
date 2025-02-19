package ma.hariti.asmaa.wrm.simulator.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.entity.enums.Role;

import java.util.List;

@Data
@Builder
public class RegisterUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    private String name;



    @NotNull(message = "Role is required")
    private Role role;

    private String password;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDTO {
        private Long id;
        private String content;
        private Float score;
        private List<String> improvementSuggestions;
        private Long questionId;
        private String feedback;
        private String followUpQuestion;
    }
}