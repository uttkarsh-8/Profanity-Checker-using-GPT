package com.profanity.profanitychecker.service;

import com.profanity.profanitychecker.model.AnalysisResult;
import org.springframework.web.multipart.MultipartFile;

public interface ProfanityAnalysisService {
    AnalysisResult analyzeText(String text);
    String generateSummaryFromModerationResult(AnalysisResult analysisResult);
    AnalysisResult analyzeAudioFile(MultipartFile audioFile);
}

