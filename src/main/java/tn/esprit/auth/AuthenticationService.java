package tn.esprit.auth;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
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
                .user(user)
                .build();

        tokenRepository.save(token);

        // Build the activation URL with the user ID as a parameter
        String activationLink = frontendActivationUrl;

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
        Token token = tokenRepository.findByToken(code)
                .orElseThrow(() -> new RuntimeException("Invalid activation code"));

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

        // Mark token as used
        token.setRevoked(true);
        token.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(token);

        log.info("Account activated for user: {}", user.getEmail());
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Verify password manually
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account not activated. Please check your email for activation instructions");
        }

        if (user.isAccountLocked()) {
            throw new BadCredentialsException("Account locked. Please contact support");
        }

        // Generate JWT token
        var jwtToken = jwtService.generateToken(user, user.getId());

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

        log.info("User logged out successfully: {}", storedToken.getUser().getEmail());
    }
}