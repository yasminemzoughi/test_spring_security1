package tn.esprit.control;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.entity.pets.Pets;

import tn.esprit.service.embede.MatchingService;

import java.util.List;
@RestController

public class MatchController {
    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Pets>> getTopMatches(@PathVariable Long userId) {
        try {
            List<Pets> topMatches = matchingService.findTopMatches(userId, 3);
            if (topMatches.isEmpty()) {
                return ResponseEntity.noContent().build();  // Return 204 if no matches
            }
            return ResponseEntity.ok(topMatches);  // Return 200 with the list of pets
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);  // Return 500 on error
        }
    }
}
