package ma.hariti.asmaa.wrm.simulator.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ma.hariti.asmaa.wrm.simulator.entity.enums.Role;

import java.util.UUID;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotNull(message = "Role is required")
    private Role role;


}
