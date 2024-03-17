package com.profanity.profanitychecker.service;

import com.profanity.profanitychecker.model.AnalysisResult;

public interface ProfanityAnalysisService {
    AnalysisResult analyzeText(String text);
    String generateSummaryFromModerationResult(AnalysisResult analysisResult);
}
