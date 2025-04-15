package tn.esprit.auth;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.dto.AuthenticationRequest;
import tn.esprit.dto.AuthenticationResponse;
import tn.esprit.dto.RegistrationRequest;
import tn.esprit.email.EmailService;
import tn.esprit.entity.*;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.TokenRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.security.JwtService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${mailing.frontend.activation-url:http://localhost:4200/activate_account}")
    private String frontendActivationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        RoleEnum roleEnum = request.getRole() != null ? request.getRole() : RoleEnum.PET_OWNER;

        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(role);
       userRepository.save(user);

        // Generate activation code and send emails
        sendActivationEmails(user);
    }

    private String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendActivationEmails(User user) throws MessagingException {
        // Generate a 6-digit activation code
        String activationCode = generateSixDigitCode();

        // Save activation code in a token
        Token token = Token.builder()
                .token(activationCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // 15 minutes expiration
                .revoked(false)
                .tokenType(TokenTypes.ACTIVATION)
                .user(user)
                .build();

        tokenRepository.save(token);
        // Send to user
        emailService.sendActivationEmail(
                user.getEmail(),
                user.getFirstName(),
                activationCode
        );
        // Send to admin
        String adminMessage = String.format(
                "New user registration: %s %s (%s)\nActivation Code: %s",
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                activationCode
        );

        emailService.sendAdminNotification(
                adminEmail,
                "New User Registration: " + user.getEmail(),
                adminMessage
        );

        log.info("Activation emails sent for user: {}", user.getEmail());
    }

    public void activateAccount(String code) {
        log.info("Attempting activation with code: {}", code);
        Token token = tokenRepository.findByTokenAndTokenType(code, TokenTypes.ACTIVATION)
                .orElseThrow(() -> {
                    log.warn("Invalid activation code: {}", code);
                    return new RuntimeException("Invalid activation code");
                });

        // Check if token is expired or revoked
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Activation code has expired");
        }
        if (token.isRevoked()) {
            throw new RuntimeException("Activation code has already been used");
        }
        // Get the user and activate their account
        User user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // Mark token as used and set isActive = true
        token.setRevoked(true);
        token.setActive(true);   // Mark account as activated

        token.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(token);

        log.info("Account activated for user: {}", user.getEmail());
    }
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        // 1. Find user
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found: {}", request.getEmail());
                    return new BadCredentialsException("Invalid credentials");
                });

        log.info("User found - enabled: {}, locked: {}",
                user.isEnabled(), user.isAccountLocked());
// Add this right before password verification
        log.info("Raw password from request: {}", request.getPassword());
        log.info("Hashed password from DB: {}", user.getPassword());
        log.info("Matches?: {}", passwordEncoder.matches(request.getPassword(), user.getPassword()));
        // 2. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Password mismatch for user: {}", request.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        // 3. Check account status
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account not activated");
        }
        if (user.isAccountLocked()) {
            throw new BadCredentialsException("Account locked");
        }

        // 4. Generate JWT
        var jwtToken = jwtService.generateToken(user, user.getId());

        // 5. Save login token
        Token loginToken = Token.builder()
                .token(jwtToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .revoked(false)
                .tokenType(TokenTypes.LOGIN_TOKEN)
                .user(user)
                .build();

        tokenRepository.save(loginToken);

        log.info("Login successful for user: {}", user.getEmail());
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public void logout(String tokenValue) {
        Token storedToken = tokenRepository.findByTokenAndTokenType(tokenValue, TokenTypes.LOGIN_TOKEN)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

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

        log.info("User logged out successfully: {}", storedToken.getUser().getEmail());
    }

    public void resetPassword(String tokenValue, String newPassword) {
        Token token = tokenRepository.findByTokenAndTokenType(tokenValue, TokenTypes.PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired token");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setRevoked(true);
        tokenRepository.save(token);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        List<Token> expiredTokens = tokenRepository.findAllByExpiresAtBefore(now);
        tokenRepository.deleteAll(expiredTokens);
        log.info("Cleaned up {} expired tokens", expiredTokens.size());
    }

}