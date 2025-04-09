package tn.esprit.auth;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.dto.AuthenticationRequest;
import tn.esprit.dto.AuthenticationResponse;
import tn.esprit.dto.RegistrationRequest;
import tn.esprit.email.EmailService;
import tn.esprit.entity.Role;
import tn.esprit.entity.RoleEnum;
import tn.esprit.entity.Token;
import tn.esprit.entity.User;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.TokenRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.security.JwtService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;

@Slf4j // Add Lombok logger

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    @Value("${spring.mailing.frontend.activation-url}")
    private String activationUrl;


    @Transactional
    public void register(RegistrationRequest request) throws MessagingException {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Get the role from repository
        Role userRole = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalStateException("Role " + request.getRole() + " not found"));

        // Create user
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(new HashSet<>())
                .build();

        // Assign role
        user.getRoles().add(userRole);

        // Save user
        userRepository.save(user);
        log.info("User created with ID: {}", user.getId());

        // Send activation email
        sendValidationEmail(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );


        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is not activated");
        }

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been sent to the same email address");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);
        return generatedToken;
    }

    private void sendValidationEmail(User user) throws MessagingException {
        try {
            var newToken = generateAndSaveActivationToken(user);
            log.info("Generated token for user {}: {}", user.getEmail(), newToken);

            emailService.sendEmail(
                    user.getEmail(),
                    user.getFullName(),
                    activationUrl,
                    newToken,
                    "Account Activation"
            );
        } catch (MessagingException e) {
            log.error("Failed to send activation email to {}", user.getEmail(), e);
            throw new EmailSendingException("Failed to send activation email");
        }
    }public static class EmailSendingException extends RuntimeException {
        public EmailSendingException(String message) {
            super(message);
        }
    }
    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            codeBuilder.append(characters.charAt(secureRandom.nextInt(characters.length())));
        }
        return codeBuilder.toString();
    }


    @Transactional
    public User createAdminUser(String firstName, String lastName, String email, String password) {
        var adminRole = roleRepository.findByName(RoleEnum.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        var user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(password))
                .accountLocked(false)
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(adminRole);
        return userRepository.save(user);
    }

}