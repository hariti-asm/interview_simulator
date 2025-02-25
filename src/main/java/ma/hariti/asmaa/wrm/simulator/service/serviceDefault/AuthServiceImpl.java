package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.request.*;
import ma.hariti.asmaa.wrm.simulator.dto.response.AuthResponse;
import ma.hariti.asmaa.wrm.simulator.dto.response.UserProfileResponse;
import ma.hariti.asmaa.wrm.simulator.entity.Admin;
import ma.hariti.asmaa.wrm.simulator.entity.Candidate;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import ma.hariti.asmaa.wrm.simulator.entity.enums.Role;
import ma.hariti.asmaa.wrm.simulator.exception.InvalidTokenException;
import ma.hariti.asmaa.wrm.simulator.exception.UserNotFoundException;
import ma.hariti.asmaa.wrm.simulator.mapper.UserMapper;
import ma.hariti.asmaa.wrm.simulator.repository.TokenRepository;
import ma.hariti.asmaa.wrm.simulator.repository.UserRepository;
import ma.hariti.asmaa.wrm.simulator.security.JwtService;
import ma.hariti.asmaa.wrm.simulator.security.UserDetailsImpl;
import ma.hariti.asmaa.wrm.simulator.service.AuthService;
import ma.hariti.asmaa.wrm.simulator.service.EmailService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMapper userMapper;
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService,
                           UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userMapper = userMapper;
    }

    @Override
    public AuthResponse login(@Valid LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String token = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            log.info("User successfully logged in: {}", user.getEmail());

            return AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .user(userMapper.toResponse(user))
                    .build();
        } catch (AuthenticationException e) {
            log.error("Login failed for user {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse loginWithRememberMe(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String token = jwtService.generateToken(userDetails, true);
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            String rememberMeToken = jwtService.generateRememberMeToken(userDetails);

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            log.info("User successfully logged in with remember-me: {}", user.getEmail());

            return AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .rememberMeToken(rememberMeToken)
                    .user(userMapper.toResponse(user))
                    .build();
        } catch (AuthenticationException e) {
            log.error("Login with remember-me failed for user {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Login with remember-me failed for user {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Login with remember-me failed: " + e.getMessage());
        }
    }

    @Override
    public void forgotPassword(@Valid ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiryDate(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            log.info("Password reset token sent to user: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    @Transactional
    public void resetPassword(@Valid ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (user.getResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiryDate(null);
        userRepository.save(user);
        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Override
    public void updatePassword(Long userId, @Valid UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidTokenException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successfully for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void registerUser(@Valid RegisterUserRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            boolean isFirstUser = userRepository.count() == 0;

            if (!isFirstUser) {  // Only check authentication if not the first user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                    throw new IllegalStateException("No authenticated user found");
                }
            } else {
                request.setRole(Role.ADMIN);  // First user must be an admin
            }

            User user = createUserByRole(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            User savedUser = userRepository.save(user);
            log.info("Created new user with role {} and email {}", request.getRole(), request.getEmail());

            emailService.sendWelcomeEmail(savedUser.getEmail(), request.getPassword());
        } catch (Exception e) {
            log.error("Failed to register user {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        try {
            String userEmail = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (jwtService.isTokenValid(refreshToken, new UserDetailsImpl(user))) {
                String token = jwtService.generateToken(new UserDetailsImpl(user));
                String newRefreshToken = jwtService.generateRefreshToken(new UserDetailsImpl(user));

                return AuthResponse.builder()
                        .token(token)
                        .refreshToken(newRefreshToken)
                        .user(userMapper.toResponse(user))
                        .build();
            }
            throw new InvalidTokenException("Invalid refresh token");
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    private void validateRoleCreation(Role currentUserRole, Role targetRole) {
        boolean isAllowed = switch (currentUserRole) {
            case ADMIN -> targetRole == Role.CANDIDATE;
            case CANDIDATE -> false;
            default -> false;
        };

        if (!isAllowed) {
            String message = String.format("User with role %s is not authorized to create users with role %s",
                    currentUserRole, targetRole);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }
    }

    private User createUserByRole(RegisterUserRequest request) {
        return switch (request.getRole()) {
            case ADMIN -> Admin.builder()
                    .email(request.getEmail())
                    .name(request.getName())
                    .role(Role.ADMIN)
                    .build();
            case CANDIDATE -> Candidate.builder()
                    .email(request.getEmail())
                    .name(request.getName())
                    .role(Role.CANDIDATE)
                    .build();
            default -> throw new IllegalArgumentException("Invalid role for registration: " + request.getRole());
        };
    }
    @Override
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return UserProfileResponse.builder()
                .email(user.getEmail())
                .firstName(user.getName())
                .build();
    }

    @Override
    public void logout(String refreshToken) {

        userRepository.deleteByResetToken(refreshToken);

        System.out.println("User logged out successfully. Refresh token invalidated.");
    }


    @Override
    public UserProfileResponse updateUserProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setName(request.getFirstName());


        userRepository.save(user);

        // Return updated profile
        return getUserProfile(email);
    }
}