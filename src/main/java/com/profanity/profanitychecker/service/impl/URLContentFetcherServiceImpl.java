package com.profanity.profanitychecker.service.impl;

import com.profanity.profanitychecker.service.URLContentFetcherService;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class URLContentFetcherServiceImpl implements URLContentFetcherService {

    private final RestTemplate restTemplate;

    @Autowired
    public URLContentFetcherServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String fetchContentFromUrl(String url) {
        try {
            String fetchedContent = restTemplate.getForObject(url, String.class);
            // Use Jsoup to parse the HTML and extract text
            return Jsoup.parse(fetchedContent).text();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch content from URL: " + url, e);
        }
    }
}
