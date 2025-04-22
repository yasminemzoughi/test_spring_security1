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

        // Validate input
        if (limit <= 0) throw new IllegalArgumentException("Limit must be positive");

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Verify user has an embedding
        if (user.getEmbeddingAsFloats() == null) {
            throw new IllegalStateException("User has no embedding");
        }

        return petsRepo.findByForAdoptionTrue().stream()
                .filter(pet -> pet.getEmbeddingAsFloats() != null) // Only pets with embeddings
                .peek(pet -> {
                    float[] petEmbedding = pet.getEmbeddingAsFloats();
                    float[] userEmbedding = user.getEmbeddingAsFloats();

                    if (petEmbedding.length != userEmbedding.length) {
                        throw new IllegalStateException("Embedding dimension mismatch");
                    }

                    pet.setSimilarityScore(
                            VectorUtils.cosineSimilarity(userEmbedding, petEmbedding)
                    );
                })
                .sorted(Comparator.comparingDouble(Pets::getSimilarityScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}