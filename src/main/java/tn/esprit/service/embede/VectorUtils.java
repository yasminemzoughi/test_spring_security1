package tn.esprit.service.embede;



//Implement similarity calculation:
public class VectorUtils {
    public static double cosineSimilarity(float[] vecA, float[] vecB) {
        // Validation
        if (vecA == null || vecB == null) {
            throw new IllegalArgumentException("Input vectors cannot be null");
        }
        if (vecA.length != vecB.length) {
            throw new IllegalArgumentException("Vectors must have equal length");
        }

        // Calculation
        float dotProduct = 0f;
        float normA = 0f;
        float normB = 0f;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}