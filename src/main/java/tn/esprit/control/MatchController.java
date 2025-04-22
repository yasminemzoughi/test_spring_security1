package tn.esprit.control;


import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.entity.pets.Pets;
import tn.esprit.service.embede.MatchingService;

import java.util.List;
@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Pets>> getTopMatches(@PathVariable Long userId) {
        return ResponseEntity.ok(matchingService.findTopMatches(userId, 3));
    }
}
