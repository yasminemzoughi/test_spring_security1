package tn.esprit.service.embede;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@Service
public class EmbeddingService {
    private final RestTemplate restTemplate;

    @Value("${app.embedding.service.url}")
    private String embeddingServiceUrl;


    public EmbeddingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public float[] getEmbedding(String text) {
        // 1. Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 2. Prepare request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);

        // 3. Create HTTP entity
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        // 4. Make the request
        ResponseEntity<Map> response = restTemplate.postForEntity(
                embeddingServiceUrl,
                request,
                Map.class
        );

        // 5. Process response
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Double> embeddingList = (List<Double>) response.getBody().get("embedding");
            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i).floatValue();
            }
            return embedding;
        } else {
            throw new RuntimeException("Failed to get embedding: " + response.getStatusCode());
        }
    }
}