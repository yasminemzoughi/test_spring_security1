package tn.esprit.auth;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.dto.AuthenticationRequest;
import tn.esprit.dto.AuthenticationResponse;
import tn.esprit.dto.RegistrationRequest;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            // Return validation errors
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(errorMessage);
        }
        try {
            // Option 1: Just register and let user login after
            authenticationService.register(request);
            return ResponseEntity.ok().build();

            // Option 2: Return token directly after registration
            // AuthenticationResponse response = authenticationService.registerAndAuthenticate(request);
            // return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @Transactional

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
        try {
            System.out.println("Attempting to authenticate: " + request.getEmail());
            var response = authenticationService.authenticate(request);
            System.out.println("Authentication successful for: " + request.getEmail());

            // Add debug logging for the generated token
            System.out.println("Generated token: " + response.getToken());

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                            "success", true,
                            "token", response.getToken(),
                            "message", "Login successful"
                    ));
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "Invalid credentials"
                    ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        authenticationService.logout(token);
        return ResponseEntity.ok("Logged out successfully.");
    }


}