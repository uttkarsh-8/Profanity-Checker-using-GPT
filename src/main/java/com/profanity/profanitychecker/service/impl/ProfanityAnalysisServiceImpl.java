package com.profanity.profanitychecker.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ProfanityAnalysisServiceImpl implements ProfanityAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Autowired
    public ProfanityAnalysisServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AnalysisResult analyzeText(String text) {
        String openAiApiUrl = "https://api.openai.com/v1/moderations";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("input", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(openAiApiUrl, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                // Map the response to the AnalysisResult class
                AnalysisResult analysisResult = objectMapper.readValue(response.getBody(), AnalysisResult.class);
                return analysisResult;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing the OpenAI API response", e);
            }
        } else {

            String errorDetails = response.getBody() != null ? response.getBody() : "No response body";
            throw new RuntimeException("Error while calling OpenAI API: " + response.getStatusCode() + " Details: " + errorDetails);
        }
    }

    @Override
    public String generateSummaryFromModerationResult(AnalysisResult analysisResult) {
        // Ensure there is a result to summarize
        if (analysisResult == null || analysisResult.getModerationResults() == null || analysisResult.getModerationResults().isEmpty()) {
            throw new IllegalArgumentException("No moderation results to summarize.");
        }

        // Chat API URL
        String chatApiUrl = "https://api.openai.com/v1/chat/completions";

        // Construct chat messages based on the moderation results
        List<Map<String, Object>> messages = new ArrayList<>();
        // Add system message (optional)
        messages.add(Map.of(
                "role", "system",
                "content", "You are an AI that summarizes content moderation results. Provide a brief summary if any content moderation categories were flagged."
        ));
        // Add user message with moderation result details
        messages.add(Map.of(
                "role", "user",
                "content", createModerationSummaryPrompt(analysisResult)
        ));

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        // Create the request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "gpt-4");
        requestBody.set("messages", objectMapper.valueToTree(messages));

        // Create the HttpEntity
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        // Make the POST request
        ResponseEntity<String> response = restTemplate.postForEntity(chatApiUrl, entity, String.class);

        // Check the response and parse the summary
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode choices = rootNode.path("choices");
                if (!choices.isEmpty()) {
                    // Assuming the summary is in the first choice and the first message
                    JsonNode messageContent = choices.get(0).path("message").path("content");
                    if (!messageContent.isMissingNode()) {
                        return messageContent.asText();
                    }
                }
                throw new RuntimeException("Failed to parse the chat API response for a summary.");
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing the chat API response", e);
            }
        } else {
            String errorDetails = response.getBody() != null ? response.getBody() : "No response body";
            throw new RuntimeException("Error while calling chat API: " + response.getStatusCode() + " Details: " + errorDetails);
        }
    }

    // moderation summary
    private String createModerationSummaryPrompt(AnalysisResult analysisResult) {
        AtomicBoolean isAnyCategoryFlagged = new AtomicBoolean(false);
        StringBuilder flaggedCategoriesSummary = new StringBuilder("The content has been analyzed. ");

        // Initialize a list to keep track of flagged categories
        List<String> flaggedCategories = new ArrayList<>();

        for (AnalysisResult.ModerationResult result : analysisResult.getModerationResults()) {
            result.getCategories().forEach((category, flagged) -> {
                if (flagged) {
                    isAnyCategoryFlagged.set(true);
                    double score = result.getCategoryScores().get(category);
                    flaggedCategories.add(String.format("'%s' (confidence score: %.2f)", category, score));
                }
            });
        }

        if (isAnyCategoryFlagged.get()) {
            // Construct a sentence listing all flagged categories
            String flaggedListString = String.join(", ", flaggedCategories);
            flaggedCategoriesSummary.append("Based on the moderation flags, the following categories were identified as problematic: ")
                    .append(flaggedListString)
                    .append(". Please review the content to ensure it aligns with community guidelines.");
        } else {
            flaggedCategoriesSummary.append("No issues were detected, and the content seems to be in compliance with community guidelines.");
        }
        return flaggedCategoriesSummary.toString();
    }
}