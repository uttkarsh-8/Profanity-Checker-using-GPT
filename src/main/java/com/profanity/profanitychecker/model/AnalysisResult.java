package com.profanity.profanitychecker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResult {
    @JsonProperty("is_explicit")
    private boolean isExplicit;
    @JsonProperty("reason")
    private String reason; // responde from GPT
    @JsonProperty("confidence_level")
    private double confidenceLevel;
}
