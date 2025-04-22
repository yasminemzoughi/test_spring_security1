package tn.esprit.service.embede;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@Service
public class EmbeddingService {
    private final RestTemplate restTemplate;

    @Value("${app.embedding.service.url}")
    private String embeddingServiceUrl;

    @Value("${app.embedding.service.timeout}")
    private int timeout;

    public EmbeddingService(RestTemplateBuilder restTemplateBuilder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout); // milliseconds
        requestFactory.setReadTimeout(timeout);    // milliseconds

        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> requestFactory)
                .build();
    }

    public float[] getEmbedding(String text) {
        try {
            // 1. Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = Map.of("text", text);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // 2. Make request to Python service
            ResponseEntity<float[]> response = restTemplate.exchange(
                    embeddingServiceUrl,
                    HttpMethod.POST,
                    request,
                    float[].class
            );

            // 3. Process response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to get embedding: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Error calling embedding service: " + e.getMessage(), e);
        }
    }
}