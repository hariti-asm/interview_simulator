package ma.hariti.asmaa.wrm.simulator.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        setFilterProcessesUrl("/api/v1/auth/login"); // Ensure this matches your frontend URL
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            // Log the incoming request body for debugging
            String requestBody = request.getReader().lines().collect(Collectors.joining());
            log.debug("Received login request body: {}", requestBody);

            // Parse the request
            LoginRequest loginRequest = objectMapper.readValue(requestBody, LoginRequest.class);

            // Store the full request for later use
            request.setAttribute("loginRequest", loginRequest);

            // Create authentication token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            // Set details from the request
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
        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();
        LoginRequest loginRequest = (LoginRequest) request.getAttribute("loginRequest");
        boolean rememberMe = loginRequest != null && loginRequest.isRememberMe();

        String token = jwtService.generateToken(userDetails, rememberMe);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        String rememberMeToken = rememberMe ? jwtService.generateRememberMeToken(userDetails) : null;

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .rememberMeToken(rememberMeToken)
                .user(userDetails.toUserResponse())
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), authResponse);
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