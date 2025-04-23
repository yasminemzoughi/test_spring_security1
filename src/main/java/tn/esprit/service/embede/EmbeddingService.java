package tn.esprit.service.embede;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import tn.esprit.dto.EmbeddingResponse;

import java.util.Map;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${app.embedding.service.url}")
    private String embeddingServiceUrl;

    public EmbeddingService(RestTemplateBuilder restTemplateBuilder,
                            @Value("${app.embedding.service.timeout:5000}") int timeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        this.restTemplate = new RestTemplate(factory);
    }

    public float[] getEmbedding(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = Map.of("text", text);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<EmbeddingResponse> response = restTemplate.exchange(
                    embeddingServiceUrl,
                    HttpMethod.POST,
                    request,
                    EmbeddingResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getEmbedding();
            }
            throw new RuntimeException("Failed to get embedding: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Error calling embedding service: " + e.getMessage(), e);
        }
    }
}
