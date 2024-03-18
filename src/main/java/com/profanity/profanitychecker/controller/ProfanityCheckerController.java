package com.profanity.profanitychecker.controller;

import com.profanity.profanitychecker.model.AnalysisResult;
import com.profanity.profanitychecker.service.FileProcessingService;
import com.profanity.profanitychecker.service.ProfanityAnalysisService;
import com.profanity.profanitychecker.service.URLContentFetcherService;
import org.springframework.http.HttpStatus;
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

    // this method is for pdf and word files
    @PostMapping("/check-file")
    public ResponseEntity<String> checkFileForProfanity(@RequestParam("file") MultipartFile file) {
        try {
            String text = fileProcessingService.extractTextFromFile(file);
            AnalysisResult analysisResult = profanityAnalysisService.analyzeText(text);
            String summary = profanityAnalysisService.generateSummaryFromModerationResult(analysisResult);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing the file: " + e.getMessage());
        }
    }

    @PostMapping("/check-url")
    public ResponseEntity<String> checkUrlForProfanity(@RequestParam("url") String url) {
        try {
            String text = urlContentFetcherService.fetchContentFromUrl(url);
            AnalysisResult analysisResult = profanityAnalysisService.analyzeText(text);
            String summary = profanityAnalysisService.generateSummaryFromModerationResult(analysisResult);
            System.out.println("HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return ResponseEntity.ok(summary);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing the URI: " + e.getMessage());
        }

    }

    @PostMapping("/check-audio")
    public ResponseEntity<String> checkAudioForProfanity(@RequestParam("audioFile")MultipartFile audioFile){
        try {
            AnalysisResult analysisResult = profanityAnalysisService.analyzeAudioFile(audioFile);
            String summary = profanityAnalysisService.generateSummaryFromModerationResult(analysisResult);
            System.out.println("IN AUDIO METHOD!!!!");
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing the audio file: " + e.getMessage());
        }
    }
}
