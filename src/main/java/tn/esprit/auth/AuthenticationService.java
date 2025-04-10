package tn.esprit.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.dto.AuthenticationRequest;
import tn.esprit.dto.AuthenticationResponse;
import tn.esprit.dto.RegistrationRequest;
import tn.esprit.entity.Role;
import tn.esprit.entity.RoleEnum;
import tn.esprit.entity.Token;
import tn.esprit.entity.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.TokenRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.security.JwtService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    public void register(RegistrationRequest request) {
        // Find the requested role or default to USER
        RoleEnum roleEnum = request.getRole() != null ? request.getRole() : RoleEnum.PET_OWNER;

        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .accountLocked(false)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(role);
        userRepository.save(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Verify password manually
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account disabled");
        }

        if (user.isAccountLocked()) {
            throw new BadCredentialsException("Account locked");
        }

        var jwtToken = jwtService.generateToken(user);

        // Save token to database
        Token token = Token.builder()
                .token(jwtToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .revoked(false)
                .user(user)
                .build();
        tokenRepository.save(token);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public void logout(String token) {
        Token storedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        // Check if token is already revoked
        if (storedToken.isRevoked()) {
            throw new RuntimeException("Token is already revoked");
        }

        // Check if token is expired
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token is already expired");
        }

        storedToken.setRevoked(true);
        storedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(storedToken);
    }


}