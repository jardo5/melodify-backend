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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    private final UserRepo userRepo;
    private final SongRepo songRepo;
    private final RecommendationRepo recommendationRepo;
    private final ObjectMapper objectMapper;

    @Autowired
    public RecommendationService(UserRepo userRepo, SongRepo songRepo, RecommendationRepo recommendationRepo, ObjectMapper objectMapper) {
        this.userRepo = userRepo;
        this.songRepo = songRepo;
        this.recommendationRepo = recommendationRepo;
        this.objectMapper = objectMapper;
    }

    public List<String> recommendSongsForUser(String userId, int limit, int offset) {
        logger.info("Fetching recommendations for user: {}", userId);
        User user = userRepo.findById(userId).orElseThrow(() -> {
            logger.error("User not found: {}", userId);
            return new RuntimeException("User not found");
        });

        Set<String> likedSongIds = new HashSet<>(user.getLikedSongs());
        Set<String> dislikedSongIds = new HashSet<>(user.getDislikedSongs());
        Set<String> savedSongIds = new HashSet<>(user.getSavedSongs());

        // Collaborative Filtering: Find similar users and recommend their liked songs
        List<User> similarUsers = findSimilarUsers(user);
        logger.info("Found {} similar users", similarUsers.size());
        Set<String> recommendedSongIds = similarUsers.stream()
                .flatMap(similarUser -> similarUser.getLikedSongs().stream())
                .filter(songId -> !likedSongIds.contains(songId) && !dislikedSongIds.contains(songId) && !savedSongIds.contains(songId))
                .collect(Collectors.toSet());

        // Content-Based Filtering: Recommend songs similar to the ones the user has liked
        likedSongIds.forEach(songId -> {
            Song song = songRepo.findById(songId).orElse(null);
            if (song != null) {
                recommendedSongIds.addAll(findSimilarSongs(song));
            }
        });

        List<String> distinctRecommendedSongIds = recommendedSongIds.stream()
                .distinct()
                .filter(songId -> !likedSongIds.contains(songId) && !dislikedSongIds.contains(songId) && !savedSongIds.contains(songId))
                .collect(Collectors.toList());

        saveRecommendations(userId, distinctRecommendedSongIds);

        // Return paginated results
        int endIndex = Math.min(offset + limit, distinctRecommendedSongIds.size());
        if (offset > endIndex) {
            return Collections.emptyList();
        }
        return distinctRecommendedSongIds.subList(offset, endIndex);
    }

    private List<User> findSimilarUsers(User user) {
        return userRepo.findAll().stream()
                .filter(otherUser -> !otherUser.getId().equals(user.getId()))
                .sorted(Comparator.comparingDouble(otherUser -> -calculatePearsonCorrelation(user, otherUser)))
                .collect(Collectors.toList());
    }

    private double calculatePearsonCorrelation(User user1, User user2) {
        Set<String> commonSongs = new HashSet<>(user1.getLikedSongs());
        commonSongs.retainAll(user2.getLikedSongs());

        int n = commonSongs.size();
        if (n == 0) return 0;

        double sum1 = commonSongs.stream().mapToDouble(songId -> 1).sum();
        double sum2 = commonSongs.stream().mapToDouble(songId -> 1).sum();

        double sum1Sq = commonSongs.stream().mapToDouble(songId -> Math.pow(1, 2)).sum();
        double sum2Sq = commonSongs.stream().mapToDouble(songId -> Math.pow(1, 2)).sum();

        double pSum = commonSongs.stream().mapToDouble(songId -> 1 * 1).sum();

        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - Math.pow(sum1, 2) / n) * (sum2Sq - Math.pow(sum2, 2) / n));
        if (den == 0) return 0;

        return num / den;
    }

    private Set<String> findSimilarSongs(Song song) {
        return songRepo.findAll().stream()
                .filter(otherSong -> !otherSong.getId().equals(song.getId()) && isSimilarSong(song, otherSong))
                .map(Song::getId)
                .collect(Collectors.toSet());
    }

    private boolean isSimilarSong(Song song1, Song song2) {
        if (song1.getArtist().equals(song2.getArtist())) {
            return true;
        }

        if (song1.getSentiment() == null || song2.getSentiment() == null) {
            return false;
        }

        try {
            JsonNode sentiment1 = objectMapper.readTree(song1.getSentiment());
            JsonNode sentiment2 = objectMapper.readTree(song2.getSentiment());

            String overallMood1 = sentiment1.path("sentiment_analysis").path("overall_mood").asText();
            String overallMood2 = sentiment2.path("sentiment_analysis").path("overall_mood").asText();

            return overallMood1.equals(overallMood2);

        } catch (IOException e) {
            logger.error("Error parsing sentiment JSON", e);
            return false;
        }
    }

    private void saveRecommendations(String userId, List<String> recommendedSongIds) {
        Recommendation existingRecommendation = recommendationRepo.findByUserId(userId).orElse(null);

        if (existingRecommendation != null) {
            existingRecommendation.setRecommendedSongIds(recommendedSongIds);
            recommendationRepo.save(existingRecommendation);
        } else {
            Recommendation recommendation = new Recommendation(userId, recommendedSongIds);
            recommendationRepo.save(recommendation);
        }
    }
}
