package tn.esprit.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.esprit.entity.pets.Pets;
import tn.esprit.repository.RoleRepository;
import tn.esprit.repository.UserRepository;
import tn.esprit.service.IUserService;
import tn.esprit.service.embede.MatchingService;

import java.util.List;
public class MatchController {
    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/user/{userId}")
    public List<Pets> getTopMatches(@PathVariable Long userId) {
        return matchingService.findTopMatches(userId, 3);
    }
}
