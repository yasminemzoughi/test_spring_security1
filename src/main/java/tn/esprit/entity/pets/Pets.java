package tn.esprit.entity.pets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Transient;
import tn.esprit.entity.user.User;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

@Setter
@Getter
@Entity
public class Pets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Information
    private String name;
    private String species;
    private int age;
    private String color;
    private String sex;

    // Adoption Details
    private boolean forAdoption;
    private String location;

    // Descriptions
    @Column(length = 2000)
    private String description;

    // Media
    private String imagePath;

    // Relationships
    private Long ownerId;

    // AI Integration
    @Column(columnDefinition = "TEXT", length = 8000)
    @Convert(converter = FloatArrayConverter.class)
    private float[] embedding;

    @Transient  // Mark as transient if not persisted
    private Double similarityScore;

    // Validated setter
    public void setSimilarityScore(double score) {
        if (score < -1 || score > 1) {
            throw new IllegalArgumentException("Score must be between -1 and 1");
        }
        this.similarityScore = score;
    }

    // Shared converter (define in your utils package)
    @Converter
    public static class FloatArrayConverter implements AttributeConverter<float[], String> {
        private static final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(float[] attribute) {
            try {
                return attribute != null ? mapper.writeValueAsString(attribute) : null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert float array to JSON", e);
            }
        }

        @Override
        public float[] convertToEntityAttribute(String dbData) {
            try {
                return dbData != null ? mapper.readValue(dbData, float[].class) : null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert JSON to float array", e);
            }
        }
    }
}