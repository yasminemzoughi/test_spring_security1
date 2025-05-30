package tn.esprit.auth;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.dto.AuthenticationRequest;
import tn.esprit.dto.AuthenticationResponse;
import tn.esprit.dto.RegistrationRequest;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

 /* @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        try {
            authenticationService.register(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
  */
 @PostMapping("/register")
 public ResponseEntity<?> register(
         @RequestBody @Valid RegistrationRequest request,
         BindingResult bindingResult  // Add this parameter
 ) throws MessagingException {
     if (bindingResult.hasErrors()) {
         // Return validation errors
         String errorMessage = bindingResult.getAllErrors().stream()
                 .map(error -> error.getDefaultMessage())
                 .collect(Collectors.joining(", "));
         return ResponseEntity.badRequest().body(errorMessage);
     }

     try {
         authenticationService.register(request);
         return ResponseEntity.ok().build();
     } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
     }
 }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
    @GetMapping("/activate-account")
    public  ResponseEntity<?>  confirm(
            @RequestParam String token
    ) throws MessagingException {
        authenticationService.activateAccount(token);
        return ResponseEntity.ok().build();
    }
}
