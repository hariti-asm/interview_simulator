package ma.hariti.asmaa.wrm.simulator.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.request.LoginRequest;
import ma.hariti.asmaa.wrm.simulator.dto.response.AuthResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        setFilterProcessesUrl("/api/v1/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            String requestBody = request.getReader().lines().collect(Collectors.joining());
            log.debug("Received login request body: {}", requestBody);

            LoginRequest loginRequest = objectMapper.readValue(requestBody, LoginRequest.class);

            request.setAttribute("loginRequest", loginRequest);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            setDetails(request, authToken);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            log.error("Failed to parse authentication request", e);
            throw new RuntimeException("Failed to parse authentication request", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        Object principal = authResult.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;

            log.info("Authenticated user: {}", userDetails.getUsername());

            // Generate JWT tokens
            LoginRequest loginRequest = (LoginRequest) request.getAttribute("loginRequest");
            boolean rememberMe = loginRequest != null && loginRequest.isRememberMe();

            String token = jwtService.generateToken(userDetails, rememberMe);
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            String rememberMeToken = rememberMe ? jwtService.generateRememberMeToken(userDetails) : null;

            // Create the authentication response
            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .rememberMeToken(rememberMeToken)
                    .user(userDetails.toUserResponse())
                    .build();

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), authResponse);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.error("Unexpected principal type: {}", principal.getClass().getName());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unexpected authentication principal type");
        }
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                Map.of("error", "Authentication failed", "message", failed.getMessage()));
    }
}