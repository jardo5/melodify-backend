package com.melodify.Melodify.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melodify.Melodify.Models.Recommendation;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.RecommendationRepo;
import com.melodify.Melodify.Repositories.SongRepo;
import com.melodify.Melodify.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private static final int MAX_RECOMMENDATIONS = 20;

    private final UserRepo userRepo;
    private final SongRepo songRepo;
    private final RecommendationRepo recommendationRepo;
    private final RecommendationCacheService cacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RecommendationService(UserRepo userRepo, SongRepo songRepo, RecommendationRepo recommendationRepo, RecommendationCacheService cacheService) {
        this.userRepo = userRepo;
        this.songRepo = songRepo;
        this.recommendationRepo = recommendationRepo;
        this.cacheService = cacheService;
    }

    public List<String> recommendSongsForUser(String userId) {
        logger.info("Fetching recommendations for user: {}", userId);

        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Generate or retrieve the full list of recommendations
        List<String> recommendedSongIds = cacheService.getRecommendationsForUser(userId);
        if (recommendedSongIds == null || recommendedSongIds.size() < MAX_RECOMMENDATIONS) {
            recommendedSongIds = generateRecommendationsForUser(user, MAX_RECOMMENDATIONS);
            cacheService.cacheRecommendationsForUser(userId, recommendedSongIds);
            saveRecommendations(userId, recommendedSongIds);
        }

        return recommendedSongIds;
    }

    private List<String> generateRecommendationsForUser(User user, int limit) {
        Set<String> likedSongIds = new HashSet<>(user.getLikedSongs());
        Set<String> dislikedSongIds = new HashSet<>(user.getDislikedSongs());
        Set<String> savedSongIds = new HashSet<>(user.getSavedSongs());

        Set<String> recommendedSongIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

        // Collaborative Filtering: Find similar users and filter out liked, disliked, and saved songs
        List<User> similarUsers = findSimilarUsers(user);
        similarUsers.parallelStream()
                .flatMap(similarUser -> similarUser.getLikedSongs().parallelStream())
                .filter(songId -> !likedSongIds.contains(songId) && !dislikedSongIds.contains(songId) && !savedSongIds.contains(songId))
                .forEach(recommendedSongIds::add);

        // Content-Based Filtering: Recommend songs similar to the ones the user has liked
        likedSongIds.parallelStream().forEach(songId -> {
            Song song = songRepo.findById(songId).orElse(null);
            if (song != null) {
                recommendedSongIds.addAll(findSimilarSongs(song, likedSongIds, dislikedSongIds, savedSongIds));
            }
        });

        // Ensure the recommendations do not include any songs that the user has liked, disliked, or saved
        recommendedSongIds.removeAll(likedSongIds);
        recommendedSongIds.removeAll(dislikedSongIds);
        recommendedSongIds.removeAll(savedSongIds);

        return recommendedSongIds.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<User> findSimilarUsers(User user) {
        return userRepo.findUsersWhoLikedSongs(user.getLikedSongs());
    }

    private Set<String> findSimilarSongs(Song song, Set<String> likedSongIds, Set<String> dislikedSongIds, Set<String> savedSongIds) {
        Set<String> similarSongs = new HashSet<>();

        // Filter songs by sentiment or artist similarity and exclude already interacted songs
        String sentiment = extractSentiment(song);
        if (sentiment != null) {
            List<String> sentimentMatches = cacheService.getSongsBySentiment(sentiment);
            if (sentimentMatches == null) {
                sentimentMatches = songRepo.findSongsBySentiment(sentiment).stream()
                        .filter(otherSong -> !otherSong.getId().equals(song.getId()))
                        .map(Song::getId)
                        .collect(Collectors.toList());
                cacheService.cacheSongsBySentiment(sentiment, sentimentMatches);
            }
            similarSongs.addAll(sentimentMatches);
        }

        if (similarSongs.isEmpty()) {
            similarSongs.addAll(songRepo.findSongsByArtistOrAlbum(song.getArtist(), song.getAlbum().getFullTitle())
                    .stream()
                    .filter(songId -> !likedSongIds.contains(songId) && !dislikedSongIds.contains(songId) && !savedSongIds.contains(songId))
                    .map(Song::getId)
                    .collect(Collectors.toSet()));
        }

        return similarSongs;
    }

    private String extractSentiment(Song song) {
        String sentimentJson = song.getSentiment();

        if (sentimentJson == null || sentimentJson.isEmpty()) {
            return null;
        }

        try {
            JsonNode sentimentNode = objectMapper.readTree(sentimentJson);
            return sentimentNode.path("sentiment_analysis").path("overall_mood").asText();
        } catch (IOException e) {
            logger.error("Failed to parse sentiment JSON for song: {}", song.getId(), e);
            return null;
        }
    }

    private void saveRecommendations(String userId, List<String> recommendedSongIds) {
        Recommendation recommendation = recommendationRepo.findByUserId(userId)
                .orElse(new Recommendation(userId, recommendedSongIds));
        recommendation.setRecommendedSongIds(recommendedSongIds);
        recommendationRepo.save(recommendation);
    }

    public void refreshRecommendationsForUser(String userId) {
        logger.info("Refreshing recommendations for user: {}", userId);

        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        cacheService.cacheRecommendationsForUser(userId, null);

        List<String> newRecommendations = generateRecommendationsForUser(user, MAX_RECOMMENDATIONS);
        cacheService.cacheRecommendationsForUser(userId, newRecommendations);

        saveRecommendations(userId, newRecommendations);
    }
}
