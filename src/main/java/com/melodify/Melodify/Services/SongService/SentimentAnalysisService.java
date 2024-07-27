package com.melodify.Melodify.Services.SongService;

import com.melodify.Melodify.Config.EnvironmentConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class SentimentAnalysisService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String SENTIMENT_API_KEY = EnvironmentConfig.SENTIMENT_API_KEY;
    private static final String SENTIMENT_PROMPT = EnvironmentConfig.SENTIMENT_PROMPT;

    private final RestTemplate restTemplate;

    @Autowired
    public SentimentAnalysisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeSentiment(String lyrics) {
        String prompt = generatePrompt(lyrics);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", new JSONArray().put(new JSONObject().put("role", "system").put("content", prompt)));
        requestBody.put("max_tokens", 500);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + SENTIMENT_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        String response = restTemplate.postForObject(API_URL, entity, String.class);
        if (response != null) {
            JSONObject responseObject = new JSONObject(response);
            String content = responseObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();
            return parseSentimentJson(content);
        } else {
            return "Failed to receive a response";
        }
    }

    public String analyzeSentimentWithTimeout(String lyrics, long timeout, TimeUnit unit) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> analyzeSentiment(lyrics));

        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            return "Sentiment analysis request timed out";
        } catch (Exception e) {
            return "Sentiment analysis failed";
        }
    }

    private String generatePrompt(String lyrics) {
        return SENTIMENT_PROMPT + "\n\nLyrics:\n" + lyrics;
    }

    private String parseSentimentJson(String jsonString) {
        try {
            // Parse the JSON string to ensure it is a valid JSON object
            JSONObject sentimentJson = new JSONObject(jsonString);

            // Convert JSONObject to a string (clean JSON without escape characters)
            return sentimentJson.toString();
        } catch (Exception e) {
            return "Invalid JSON response";
        }
    }
}
