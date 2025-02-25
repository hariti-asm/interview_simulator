package ma.hariti.asmaa.wrm.simulator.service;

import jakarta.validation.Valid;
import ma.hariti.asmaa.wrm.simulator.dto.request.*;
import ma.hariti.asmaa.wrm.simulator.dto.response.AuthResponse;
import ma.hariti.asmaa.wrm.simulator.dto.response.UserProfileResponse;

import java.util.UUID;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse loginWithRememberMe(LoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void updatePassword(Long userId, @Valid UpdatePasswordRequest request);
    AuthResponse refreshToken(String refreshToken);
    void registerUser(RegisterUserRequest request);
    UserProfileResponse getUserProfile(String email);
    void logout(String refreshToken);
    UserProfileResponse updateUserProfile(String email, UpdateProfileRequest request);
}