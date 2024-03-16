package com.profanity.profanitychecker.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileProcessingService {
    String extractTextFromFile(MultipartFile file);
}
