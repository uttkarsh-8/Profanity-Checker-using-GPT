package com.profanity.profanitychecker.controller;

import com.profanity.profanitychecker.model.AnalysisResult;
import com.profanity.profanitychecker.service.FileProcessingService;
import com.profanity.profanitychecker.service.ProfanityAnalysisService;
import com.profanity.profanitychecker.service.URLContentFetcherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profanity-check")
public class ProfanityCheckerController {

    private final FileProcessingService fileProcessingService;
    private final ProfanityAnalysisService profanityAnalysisService;
    private final URLContentFetcherService urlContentFetcherService;

    public ProfanityCheckerController(FileProcessingService fileProcessingService, ProfanityAnalysisService profanityAnalysisService, URLContentFetcherService urlContentFetcherService) {
        this.fileProcessingService = fileProcessingService;
        this.profanityAnalysisService = profanityAnalysisService;
        this.urlContentFetcherService = urlContentFetcherService;
    }

    @PostMapping("/check-file")
    public ResponseEntity<AnalysisResult> checkFileForProfanity(@RequestParam("file") MultipartFile file) {

        String text = fileProcessingService.extractTextFromFile(file);
        AnalysisResult analysisResult = profanityAnalysisService.analyzeText(text);

        return ResponseEntity.ok(analysisResult);
    }

    @PostMapping("/check-url")
    public ResponseEntity<AnalysisResult> checkUrlForProfanity(@RequestParam("url") String url) {
        String text = urlContentFetcherService.fetchContentFromUrl(url);
        AnalysisResult analysisResult = profanityAnalysisService.analyzeText(text);
        return ResponseEntity.ok(analysisResult);
    }
}
