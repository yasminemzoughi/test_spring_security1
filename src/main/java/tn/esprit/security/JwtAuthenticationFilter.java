package tn.esprit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
 * JWT Authentication Filter that processes incoming requests and validates JWT tokens.
 * This filter is applied once per request and handles both public and secured endpoints.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // List of public endpoints that don't require JWT authentication
    private static final List<String> WHITELIST = List.of(
            "/auth/register",       // User registration endpoint
            "/auth/login",           // User login endpoint
            "/auth/authenticate",    // Authentication endpoint
            "/auth/activate-account" // Account activation endpoint
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println(">>> Servlet path: " + request.getServletPath());

        String path = request.getServletPath();

        // Skip JWT filter for public (unauthenticated) paths
        if (WHITELIST.contains(path)) {
            // If path is in whitelist, continue with next filters without authentication
            filterChain.doFilter(request, response);
            return;
        }

        // Get Authorization header from request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check if Authorization header is missing or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Continue filter chain without authentication (will likely result in 401)
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from Authorization header (remove "Bearer " prefix)
        jwt = authHeader.substring(7);
        // Extract username/email from JWT token
        userEmail = jwtService.extractUsername(jwt);

        // If username is extracted and there's no existing authentication in the context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details from database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Validate if the token is valid for the loaded user details
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Create authentication token with user details and authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // credentials are null as we're using JWT
                        userDetails.getAuthorities() // user roles/permissions
                );

                // Add request details to the authentication token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue with the next filters in the chain
        filterChain.doFilter(request, response);
    }
}