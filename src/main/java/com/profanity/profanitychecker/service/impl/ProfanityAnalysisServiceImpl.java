package com.profanity.profanitychecker.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.profanity.profanitychecker.model.AnalysisResult;
import com.profanity.profanitychecker.service.ProfanityAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProfanityAnalysisServiceImpl implements ProfanityAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}") // Assuming you have defined this in your application.properties or as an environment variable
    private String openAiApiKey;

    @Autowired
    public ProfanityAnalysisServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AnalysisResult analyzeText(String text) {
        String openAiApiUrl = "https://api.openai.com/v1/completions";

        // Constructing the request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("prompt", "Given the following text, determine if it contains any profanity or explicit content: '" + text + "'");
        requestBody.put("max_tokens", 50);
        requestBody.put("temperature", 0.5);


        // Set up headers with the required authentication token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey); // Now using the injected API key

        // Create an HttpEntity object with the headers and the request body
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        // Execute the POST request to the OpenAI API
        ResponseEntity<String> response = restTemplate.postForEntity(openAiApiUrl, entity, String.class);

        // Check the response status code and process the response body accordingly
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                // Assuming the actual response parsing to AnalysisResult is more complex and adjusted as per the actual API response structure
                AnalysisResult analysisResult = objectMapper.readValue(response.getBody(), AnalysisResult.class);

                return analysisResult;

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing the OpenAI API response", e);
            }
        } else {
            // Handle error scenario
            throw new RuntimeException("Error while calling OpenAI API: " + response.getStatusCode());
        }
    }
}