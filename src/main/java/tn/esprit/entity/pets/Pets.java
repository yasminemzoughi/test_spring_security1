package tn.esprit.entity.pets;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Transient;
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
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] embedding;

    private Double similarityScore;

    // Helper method to set score
    public void setSimilarityScore(double score) {
        this.similarityScore = score;
    }

    // Helper Methods for Embeddings
    public void setEmbedding(float[] floats) {
        this.embedding = floatToByteArray(floats);
    }

    @Transient
    public float[] getEmbeddingAsFloats() {
        return byteToFloatArray(this.embedding);
    }

    // Static conversion utilities
    public static byte[] floatToByteArray(float[] floats) {
        if (floats == null) return null;
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * Float.BYTES);
        buffer.asFloatBuffer().put(floats);
        return buffer.array();
    }

    public static float[] byteToFloatArray(byte[] bytes) {
        if (bytes == null) return null;
        FloatBuffer buffer = ByteBuffer.wrap(bytes).asFloatBuffer();
        float[] floats = new float[buffer.remaining()];
        buffer.get(floats);
        return floats;
    }


}
