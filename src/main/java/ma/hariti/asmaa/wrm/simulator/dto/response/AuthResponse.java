package ma.hariti.asmaa.wrm.simulator.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String rememberMeToken;
    private UserResponse user;
}
