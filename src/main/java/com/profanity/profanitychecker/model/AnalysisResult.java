package com.profanity.profanitychecker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResult {
    private String id;
    private String model;

    @JsonProperty("results")
    private List<ModerationResult> moderationResults;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModerationResult {
        @JsonProperty("flagged")
        private boolean flagged;

        @JsonProperty("categories")
        private Map<String, Boolean> categories; // Maps category names to whether they were flagged

        @JsonProperty("category_scores")
        private Map<String, Double> categoryScores; // Maps category names to confidence scores
    }
}
