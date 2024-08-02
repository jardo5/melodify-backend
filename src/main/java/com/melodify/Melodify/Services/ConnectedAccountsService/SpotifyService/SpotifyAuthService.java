package com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService;

import com.melodify.Melodify.Config.EnvironmentConfig;
import com.melodify.Melodify.Models.ConnectedAccount;
import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.UserRepo;
import com.melodify.Melodify.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;

@Service
public class SpotifyAuthService {

    private final String clientId = EnvironmentConfig.SPOTIFY_CLIENT_ID;
    private final String clientSecret = EnvironmentConfig.SPOTIFY_CLIENT_SECRET;
    private final String redirectUri = EnvironmentConfig.SPOTIFY_REDIRECT_URI;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String currentState;

    // Generates the Spotify authorization URL
    public String getAuthorizationUrl() {
        currentState = generateRandomString(16);
        String scope = "user-read-private user-read-email playlist-read-private playlist-read-collaborative";

        return "https://accounts.spotify.com/authorize?" +
                "response_type=code&client_id=" + clientId +
                "&scope=" + scope +
                "&redirect_uri=" + redirectUri +
                "&state=" + currentState;
    }

    // Handles the Spotify callback and stores tokens
    public void handleSpotifyCallback(String code, String state, String jwtToken) {
        if (!currentState.equals(state)) {
            throw new IllegalStateException("Invalid state parameter");
        }

        String authHeader = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity("https://accounts.spotify.com/api/token", entity, Map.class);
            System.out.println("Response: " + response);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                String accessToken = (String) responseBody.get("access_token");
                String refreshToken = (String) responseBody.get("refresh_token");
                Integer expiresIn = (Integer) responseBody.get("expires_in");

                // Calculate the expiration time as ISO 8601 formatted string
                String expiresAt = calculateIso8601Expiry(expiresIn);

                String username = extractUsernameFromJwt(jwtToken);
                Optional<User> userOptional = userRepository.findByUsername(username);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    saveTokensToUser(user, accessToken, refreshToken, expiresAt);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in handleSpotifyCallback: " + e.getMessage());
            throw new RuntimeException("Error processing Spotify callback", e);
        }
    }

    // Ensures valid Spotify access token, refreshes if expired
    public String getValidSpotifyAccessToken(User user) {
        ConnectedAccount spotifyAccount = getConnectedAccount(user, "spotify");

        if (spotifyAccount == null) {
            throw new RuntimeException("No Spotify account connected");
        }

        if (isTokenExpired(spotifyAccount)) {
            refreshSpotifyToken(user);
            spotifyAccount = getConnectedAccount(user, "spotify"); // Refresh the account after token update
        }

        return spotifyAccount.getAccessToken();
    }

    // Refreshes Spotify access token using the refresh token
    public void refreshSpotifyToken(User user) {
        ConnectedAccount spotifyAccount = getConnectedAccount(user, "spotify");

        if (spotifyAccount != null && spotifyAccount.getRefreshToken() != null) {
            String authHeader = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", spotifyAccount.getRefreshToken());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            try {
                ResponseEntity<Map> response = restTemplate.postForEntity("https://accounts.spotify.com/api/token", entity, Map.class);
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    String newAccessToken = (String) responseBody.get("access_token");
                    Integer expiresIn = (Integer) responseBody.get("expires_in");
                    String expiresAt = calculateIso8601Expiry(expiresIn);

                    spotifyAccount.setAccessToken(newAccessToken);
                    spotifyAccount.setExpiresAt(expiresAt);
                    userRepository.save(user);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error refreshing Spotify token", e);
            }
        } else {
            throw new RuntimeException("No refresh token available for Spotify");
        }
    }

    // Checks if the token is expired
    public boolean isTokenExpired(ConnectedAccount connectedAccount) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date expiryDate = sdf.parse(connectedAccount.getExpiresAt());
            return expiryDate.before(new Date());
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing expiry date", e);
        }
    }

    // Retrieves connected account for a given provider
    public ConnectedAccount getConnectedAccount(User user, String provider) {
        return user.getConnectedAccounts().stream()
                .filter(account -> account.getProvider().equals(provider))
                .findFirst()
                .orElse(null);
    }

    // Saves access and refresh tokens to user, ensuring no duplicates
    private void saveTokensToUser(User user, String accessToken, String refreshToken, String expiresAt) {
        Optional<ConnectedAccount> existingAccountOpt = user.getConnectedAccounts().stream()
                .filter(account -> account.getProvider().equals("spotify"))
                .findFirst();

        if (existingAccountOpt.isPresent()) {
            ConnectedAccount existingAccount = existingAccountOpt.get();
            existingAccount.setAccessToken(accessToken);
            existingAccount.setRefreshToken(refreshToken);
            existingAccount.setExpiresAt(expiresAt);
        } else {
            ConnectedAccount connectedAccount = new ConnectedAccount();
            connectedAccount.setProvider("spotify");
            connectedAccount.setAccessToken(accessToken);
            connectedAccount.setRefreshToken(refreshToken);
            connectedAccount.setExpiresAt(expiresAt);

            user.getConnectedAccounts().add(connectedAccount);
        }

        userRepository.save(user);
    }

    // Generates a random string of given length
    private String generateRandomString(int length) {
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(possible.charAt(random.nextInt(possible.length())));
        }
        return sb.toString();
    }

    // Extracts username from JWT token
    private String extractUsernameFromJwt(String jwtToken) {
        return jwtUtil.extractUsername(jwtToken);
    }

    // Calculates ISO 8601 formatted expiry date from expiresIn value
    private String calculateIso8601Expiry(int expiresIn) {
        Date expiryDate = new Date(System.currentTimeMillis() + (expiresIn * 1000L));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(expiryDate);
    }
}
