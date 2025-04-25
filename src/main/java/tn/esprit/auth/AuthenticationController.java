package tn.esprit.auth;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.dto.auth.AuthenticationRequest;
import tn.esprit.dto.auth.RegistrationRequest;
import tn.esprit.entity.user.User;
import tn.esprit.repository.UserRepository;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
private final UserRepository userRepository;
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", errorMessage)
            );
        }

        try {
            authenticationService.register(request);
            return ResponseEntity.ok().body(
                    Map.of("success", true, "message", "Registration successful. Please check your email for activation instructions.")
            );
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Failed to send activation email")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }
    @Transactional
    @PostMapping("/activate")
    public ResponseEntity<?> activateAccount(@RequestBody Map<String, String> request) {
        try {
            authenticationService.activateAccount(request.get("code"));
            return ResponseEntity.ok().body(
                    Map.of("success", true, "message", "Account activated successfully!")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        try {
            var response = authenticationService.authenticate(request);
            return ResponseEntity.ok()
                    .body(Map.of(
                            "success", true,
                            "token", response.getToken(),
                            "message", "Login successful"
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            authenticationService.logout(token);
            return ResponseEntity.ok().body(
                    Map.of("success", true, "message", "Logged out successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Email is required")
            );
        }

        // Check if the email exists in the database
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // If the email doesn't exist, return a specific message
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Email does not exist")
            );
        }

        try {
            // If the user exists, initiate password reset
            authenticationService.initiatePasswordReset(email);
            return ResponseEntity.ok().body(
                    Map.of("success", true, "message", "password reset instructions have been sent to your email")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "An error occurred while processing the request")
            );
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            authenticationService.resetPassword(token, newPassword);
            return ResponseEntity.ok().body(
                    Map.of("success", true, "message", "Password reset successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }
}