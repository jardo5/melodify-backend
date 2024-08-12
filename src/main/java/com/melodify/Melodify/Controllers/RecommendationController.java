package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.UserRepo;
import com.melodify.Melodify.Services.RecommendationService;
import com.melodify.Melodify.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;
    private final UserRepo userRepo;
    private final JwtUtil jwtUtil;

    @Autowired
    public RecommendationController(RecommendationService recommendationService, UserRepo userRepo, JwtUtil jwtUtil) {
        this.recommendationService = recommendationService;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<String>> getRecommendations(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            logger.error("Authorization token is missing or invalid");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            String jwtToken = jwtUtil.extractToken(token); // Extract the JWT token
            String username = jwtUtil.extractUsername(jwtToken); // Extract the username
            logger.info("Extracted username from token: {}", username);
            User user = userRepo.findByUsername(username).orElseThrow(() -> {
                logger.error("User not found: {}", username);
                return new RuntimeException("User not found");
            });
            List<String> recommendations = recommendationService.recommendSongsForUser(user.getId());
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Failed to fetch recommendations", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<String>> refreshRecommendations(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            logger.error("Authorization token is missing or invalid");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            String jwtToken = jwtUtil.extractToken(token); // Extract the JWT token
            String username = jwtUtil.extractUsername(jwtToken); // Extract the username
            logger.info("Extracted username from token: {}", username);
            User user = userRepo.findByUsername(username).orElseThrow(() -> {
                logger.error("User not found: {}", username);
                return new RuntimeException("User not found");
            });

            // Refresh the recommendations for the user
            recommendationService.refreshRecommendationsForUser(user.getId());

            // Fetch the new recommendations to return
            List<String> refreshedRecommendations = recommendationService.recommendSongsForUser(user.getId());
            return ResponseEntity.ok(refreshedRecommendations);
        } catch (Exception e) {
            logger.error("Failed to refresh recommendations", e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
