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
import tn.esprit.entity.Token;
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
            "/auth/register",
            "/api/auth/register",
            "/auth/login"
    );

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

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, "Missing or invalid Authorization header", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final String jwt = authHeader.substring(7);

        if (jwt.split("\\.").length != 3) {
            sendError(response, "Invalid token structure", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            // 1. Find token in DB
            Token storedToken = tokenRepository.findByToken(jwt)
                    .orElseThrow(() -> new RuntimeException("Token not found in database"));

            if (storedToken.isRevoked()) {
                sendError(response, "Token has been revoked", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                sendError(response, "Token has expired", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 2. Extract username from JWT
            final String userEmail = jwtService.extractUsername(jwt);
            if (userEmail == null) {
                sendError(response, "Unable to extract user from token", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 3. If not already authenticated, do it
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authenticated user: {}", userEmail);
                } else {
                    sendError(response, "Invalid token", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
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
