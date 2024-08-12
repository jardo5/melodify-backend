package com.melodify.Melodify.Services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationCacheService {

    private final Map<String, List<String>> sentimentCache;
    private final Map<String, List<String>> recommendationCache;

    public RecommendationCacheService() {
        this.sentimentCache = new HashMap<>();
        this.recommendationCache = new HashMap<>();
    }

    public List<String> getSongsBySentiment(String sentiment) {
        return sentimentCache.get(sentiment);
    }

    public void cacheSongsBySentiment(String sentiment, List<String> songIds) {
        sentimentCache.put(sentiment, songIds);
    }

    public List<String> getRecommendationsForUser(String userId) {
        return recommendationCache.get(userId);
    }

    public void cacheRecommendationsForUser(String userId, List<String> recommendations) {
        recommendationCache.put(userId, recommendations);
    }
}
