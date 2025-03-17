package ma.hariti.asmaa.wrm.simulator.controller;

import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.UserDTO;
import ma.hariti.asmaa.wrm.simulator.security.UserDetailsImpl;
import ma.hariti.asmaa.wrm.simulator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    private Long extractAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("No authentication found in security context");
            throw new IllegalStateException("User must be authenticated");
        }

        log.debug("Authentication principal type: {}",
                authentication.getPrincipal().getClass().getName());

        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            if (userId == null) {
                log.error("UserDetailsImpl found but ID is null");
                throw new IllegalStateException("User ID not found in authentication");
            }

            log.debug("Extracted user ID: {} from UserDetailsImpl", userId);
            return userId;
        }

        log.error("Unsupported principal type: {}",
                authentication.getPrincipal().getClass().getName());
        throw new IllegalStateException("Unsupported authentication type");
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            log.info("Creating new user: {}", userDTO);
            UserDTO createdUser = userService.createUser(userDTO);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create user: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Long authenticatedUserId = extractAuthenticatedUserId();
            log.info("Authenticated user {} requesting user data for ID: {}", authenticatedUserId, id);

            if (!id.equals(authenticatedUserId)) {
                log.warn("User {} attempted to access data for user {}", authenticatedUserId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "success", false, "status", 403));
            }

            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (Exception e) {
            log.error("Error getting user by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get user: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            Long authenticatedUserId = extractAuthenticatedUserId();
            log.info("User {} requesting all users", authenticatedUserId);

            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (Exception e) {
            log.error("Error getting all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get users: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            Long authenticatedUserId = extractAuthenticatedUserId();
            log.info("User {} attempting to update user {}", authenticatedUserId, id);

            if (!id.equals(authenticatedUserId)) {
                log.warn("User {} attempted to update data for user {}", authenticatedUserId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "success", false, "status", 403));
            }

            userDTO.setId(id);
            UserDTO updatedUser = userService.updateUser(userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (Exception e) {
            log.error("Error updating user: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update user: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            Long authenticatedUserId = extractAuthenticatedUserId();
            log.info("User {} attempting to delete user {}", authenticatedUserId, id);

            if (!id.equals(authenticatedUserId)) {
                log.warn("User {} attempted to delete user {}", authenticatedUserId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "success", false, "status", 403));
            }

            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully",
                    "success", true, "status", 200));
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (Exception e) {
            log.error("Error deleting user: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete user: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping("/{id}/interviews")
    public ResponseEntity<?> getUserInterviews(@PathVariable Long id) {
        try {
            Long authenticatedUserId = extractAuthenticatedUserId();
            log.info("User {} requesting interviews for user {}", authenticatedUserId, id);

            if (!id.equals(authenticatedUserId)) {
                log.warn("User {} attempted to access interviews for user {}", authenticatedUserId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "success", false, "status", 403));
            }

            List<InterviewSessionDTO> interviews = userService.getUserInterviews(id);
            return ResponseEntity.ok(interviews);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (Exception e) {
            log.error("Error getting interviews for user: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get interviews: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }

    @GetMapping("/{id}/performance")
    public ResponseEntity<?> getUserPerformance(@PathVariable Long id) {
        try {
            Long authenticatedUserId = extractAuthenticatedUserId();
            log.info("User {} requesting performance data for user {}", authenticatedUserId, id);

            if (!id.equals(authenticatedUserId)) {
                log.warn("User {} attempted to access performance data for user {}", authenticatedUserId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied", "success", false, "status", 403));
            }

            Object performance = userService.getUserPerformance(id);
            return ResponseEntity.ok(performance);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "success", false, "status", 401));
        } catch (Exception e) {
            log.error("Error getting performance data for user: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get performance data: " + e.getMessage(),
                            "success", false, "status", 500));
        }
    }
}

