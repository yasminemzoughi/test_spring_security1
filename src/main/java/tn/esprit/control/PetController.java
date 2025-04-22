package tn.esprit.control;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.entity.pets.Pets;
import tn.esprit.repository.PetsRepository;
import tn.esprit.service.Pet.IPetService;
import tn.esprit.service.embede.EmbeddingService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/pets")
public class PetController {

    @Autowired
    private IPetService petService;
    private  final PetsRepository petsRepository;
    private final EmbeddingService embeddingService;


    // Get all pets
    @GetMapping("get_all_pets")
    public List<Pets> getAllPets() {
        return petService.getAllPets();
    }

    // Get pet by ID
    @GetMapping("get_pet_by_id/{id}")
    public ResponseEntity<Pets> getPetById(@PathVariable Long id) {
        Pets pet = petService.getPetsById(id);
        return pet != null ? ResponseEntity.ok(pet) : ResponseEntity.notFound().build();
    }

    // Create new pet
    @PostMapping("add_pet")
    public Pets createPet(@RequestBody Pets pet) {
        return petService.createPets(pet);
    }

    // Update pet
    @PutMapping("update_pet/{id}")
    public ResponseEntity<Pets> updatePet(@PathVariable Long id, @RequestBody Pets updatedPet) {
        Pets pet = petService.updatePets(id, updatedPet);
        return pet != null ? ResponseEntity.ok(pet) : ResponseEntity.notFound().build();
    }

    // Delete pet
    @DeleteMapping("delete_pet/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePets(id);
        return ResponseEntity.noContent().build();
    }

}
