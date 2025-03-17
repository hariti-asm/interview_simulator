package ma.hariti.asmaa.wrm.simulator.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String requestPath = request.getRequestURI();

        // Print all request headers for debugging
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.debug("Header: {} = {}", headerName, request.getHeader(headerName));
        }

        log.info("Processing request for path: {}", requestPath);

        // Only skip authentication for public endpoints
        if (shouldSkipAuthentication(requestPath)) {
            log.debug("Skipping authentication for public path: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // For protected endpoints, require authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No valid authorization header found for protected path: {}", requestPath);
            sendUnauthorizedResponse(response, "Authentication required");
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            log.debug("JWT token: {}", jwt);

            final String userEmail = jwtService.extractUsername(jwt);
            log.info("Extracted username from token: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                log.info("Loaded user details for: {}", userEmail);
                log.info("User authorities: {}", userDetails.getAuthorities());

                if (userDetails instanceof UserDetailsImpl) {
                    log.info("User ID: {}", ((UserDetailsImpl) userDetails).getId());
                }

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    log.info("Authentication successful for user: {}", userEmail);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    log.warn("Token validation failed for user: {}", userEmail);
                    sendUnauthorizedResponse(response, "Invalid token");
                    return;
                }
            } else {
                log.warn("Could not extract username from token or authentication already exists");
                sendUnauthorizedResponse(response, "Invalid token format");
                return;
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage(), e);
            sendUnauthorizedResponse(response, "JWT processing error: " + e.getMessage());
            return;
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\",\"status\":401,\"success\":false}");
    }

    private boolean shouldSkipAuthentication(String requestPath) {
        return requestPath != null && (
                requestPath.startsWith("/api/v1/auth/register") ||
                        requestPath.startsWith("/api/v1/auth/login") ||
                        requestPath.startsWith("/api/v1/auth/forgot-password") ||
                        requestPath.startsWith("/api/v1/auth/reset-password") ||
                        requestPath.startsWith("/api/v1/auth/refresh-token")
        );
    }
}

