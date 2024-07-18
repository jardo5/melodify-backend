package com.melodify.Melodify.Services.SongService;

import com.melodify.Melodify.Config.RestTemplateConfig;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SentimentAnalysisService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String SENTIMENT_API_KEY = RestTemplateConfig.SENTIMENT_API_KEY;
    private static final String SENTIMENT_PROMPT = RestTemplateConfig.SENTIMENT_PROMPT;

    private final RestTemplate restTemplate;

    @Autowired
    public SentimentAnalysisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeSentiment(String lyrics) {
        String prompt = generatePrompt(lyrics);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", new org.json.JSONArray().put(new JSONObject().put("role", "system").put("content", prompt)));
        requestBody.put("max_tokens", 500);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + SENTIMENT_API_KEY);

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody.toString(), headers);

        String response = restTemplate.postForObject(API_URL, entity, String.class);
        JSONObject responseObject = new JSONObject(response);
        return responseObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();
    }

    private String generatePrompt(String lyrics) {
        return SENTIMENT_PROMPT + "\n\nLyrics:\n" + lyrics;
    }
}

//TODO: Make sentiment analysis API call when a specific song is searched
//TODO: Add error handling for when the sentiment analysis API is down
