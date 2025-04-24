package tn.esprit.control;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.dto.matching.MatchResponseDTO;
import tn.esprit.service.matching.IMatchingService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final IMatchingService matchingService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<MatchResponseDTO> getMatchesForUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer topN) {
        try {
            MatchResponseDTO matches = matchingService.matchPetsToUser(userId, topN);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}