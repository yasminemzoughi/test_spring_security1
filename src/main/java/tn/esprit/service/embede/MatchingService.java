package tn.esprit.service.embede;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.entity.pets.Pets;
import tn.esprit.entity.user.User;
import tn.esprit.repository.PetsRepository;
import tn.esprit.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {
    private final UserRepository userRepo;
    private final PetsRepository petsRepo;
    private final EmbeddingService embeddingService;

    public List<Pets> findTopMatches(Long userId, int limit) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getBio() == null || user.getBio().isEmpty() || user.getBioEmbedding() == null) {
            return getFallbackPets(limit);
        }

        return petsRepo.findByForAdoptionTrue().stream()
                .filter(pet -> pet.getDescription() != null && !pet.getDescription().isEmpty())
                .filter(pet -> pet.getEmbedding() != null)  // Now checks float[] directly
                .peek(pet -> {
                    float[] petEmbedding = pet.getEmbedding();  // Direct access
                    float[] userEmbedding = user.getBioEmbedding();
                    pet.setSimilarityScore(
                            VectorUtils.cosineSimilarity(userEmbedding, petEmbedding)
                    );
                })
                .sorted(Comparator.comparingDouble(Pets::getSimilarityScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<Pets> getFallbackPets(int limit) {
        return petsRepo.findRandomPets(limit);
    }
}