package tn.esprit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.esprit.entity.token.Token;
import tn.esprit.entity.token.TokenTypes;
import tn.esprit.repository.TokenRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/activate",
            "/api/user/images/**",
           "/api/pets/**",
            "/api/auth/forgot-password",
            "/api/auth/reset-password");
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/user/images/") || path.startsWith("/api/pets/") ;
    }
        @Override
        protected void doFilterInternal(
                @NonNull HttpServletRequest request,
                @NonNull HttpServletResponse response,
                @NonNull FilterChain filterChain
        ) throws ServletException, IOException {

            final String requestURI = request.getRequestURI();
            log.debug("Processing request for: {} {}", request.getMethod(), requestURI);

            if (isPublicEndpoint(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");

            // 1. Validate Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(response, "Missing or invalid Authorization header", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            final String jwt = authHeader.substring(7);
            try {
                // 2. First verify token exists in database
                Token storedToken = tokenRepository.findByTokenAndTokenType(jwt, TokenTypes.LOGIN_TOKEN)
                        .orElseThrow(() -> {
                            log.error("Login token not found in database for JWT: {}", jwt);
                            return new RuntimeException("Invalid or expired session");
                        });

                // 3. Check token validity
                if (storedToken.isRevoked()) {
                    log.warn("Attempt to use revoked token for user: {}", storedToken.getUser().getEmail());
                    sendError(response, "Session has been terminated", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                    log.warn("Expired token attempt for user: {}", storedToken.getUser().getEmail());
                    sendError(response, "Session has expired", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                // 4. Extract and validate user
                final String userEmail = jwtService.extractUsername(jwt);
                if (userEmail == null) {
                    log.error("Could not extract username from JWT: {}", jwt);
                    sendError(response, "Invalid token payload", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // 5. Check if user needs authentication
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                    // Additional check that token belongs to this user
                    if (!storedToken.getUser().getEmail().equals(userEmail)) {
                        log.error("Token user mismatch. Token user: {}, JWT user: {}",
                                storedToken.getUser().getEmail(), userEmail);
                        sendError(response, "Token-user mismatch", HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("Authenticated user: {}", userEmail);
                    } else {
                        log.warn("Invalid JWT signature for user: {}", userEmail);
                        sendError(response, "Invalid token signature", HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }

                filterChain.doFilter(request, response);

            } catch (Exception e) {
                log.error("Authentication error for URI {}: {}", requestURI, e.getMessage());
                sendError(response, "Authentication failed: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
            }
        }

    private boolean isPublicEndpoint(String requestURI) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(publicPath ->
                requestURI.equals(publicPath) || requestURI.startsWith(publicPath + "/")
        );
    }

    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        log.warn("Authentication failed: {}", message);
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(
                String.format("{\"error\": \"%s\", \"status\": %d}", message, status)
        );
    }
}
