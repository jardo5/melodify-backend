package com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService;

import com.melodify.Melodify.Config.EnvironmentConfig;
import com.melodify.Melodify.DTOs.SpotifyPlaylistsResponseDTO;
import com.melodify.Melodify.DTOs.SpotifySongsResponseDTO;
import com.melodify.Melodify.Models.Album;
import com.melodify.Melodify.Models.Playlist;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Models.TopTrack;
import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.TopTrackRepo;
import com.melodify.Melodify.Repositories.UserRepo;
import com.melodify.Melodify.Utils.JwtUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotifyApiService {

    private static final long SYNC_INTERVAL = 3600_000; // 1 hour
    private static final String CLIENT_ID = EnvironmentConfig.SPOTIFY_CLIENT_ID;
    private static final String CLIENT_SECRET = EnvironmentConfig.SPOTIFY_CLIENT_SECRET;
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String PLAYLIST_URL = "https://api.spotify.com/v1/playlists/37i9dQZEVXbLRQDuF5jeBp/tracks"; // Top 50 USA Playlist

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private SpotifyAuthService spotifyAuthService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TopTrackRepo topTrackRepo;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SpotifyApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // General method to handle Spotify API calls
    public <T> T handleSpotifyApiCall(String bearerToken, String endpoint, HttpMethod method, ParameterizedTypeReference<T> responseType) {
        String token = jwtUtil.extractToken(bearerToken); // Extract token from bearer token
        String username = jwtUtil.extractUsername(token); // Extract username from token
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        String accessToken = spotifyAuthService.getValidSpotifyAccessToken(user); // Get valid Spotify access token

        return executeApiCall(accessToken, endpoint, method, responseType);
    }

    // Executes the API call using the provided access token and returns the response
    private <T> T executeApiCall(String accessToken, String endpoint, HttpMethod method, ParameterizedTypeReference<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<T> response = restTemplate.exchange(
                endpoint,
                method,
                entity,
                responseType
        );

        return response.getBody();
    }

    // Specific method to get user playlists using the general API call handler
    public List<Playlist> getUserPlaylists(String bearerToken) {
        String token = jwtUtil.extractToken(bearerToken); // Extract token from bearer token
        String username = jwtUtil.extractUsername(token); // Extract username from token
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Check if we need to update the playlists
        if (user.getLastPlaylistSync() == null || new Date().getTime() - user.getLastPlaylistSync().getTime() > SYNC_INTERVAL) {
            String endpoint = "https://api.spotify.com/v1/me/playlists";
            ParameterizedTypeReference<SpotifyPlaylistsResponseDTO> responseType = new ParameterizedTypeReference<>() {};
            SpotifyPlaylistsResponseDTO spotifyResponse = handleSpotifyApiCall(bearerToken, endpoint, HttpMethod.GET, responseType);
            List<Playlist> playlists = spotifyResponse.getItems().stream()
                    .map(item -> mapToPlaylist(item, bearerToken))
                    .collect(Collectors.toList());

            savePlaylistsToUser(user, playlists);
        }

        return user.getPlaylists();
    }

    // Maps SpotifyPlaylistItem to Playlist and fetches songs
    private Playlist mapToPlaylist(SpotifyPlaylistsResponseDTO.SpotifyPlaylistItem item, String bearerToken) {
        Playlist playlist = new Playlist();
        playlist.setId(item.getId());
        playlist.setName(item.getName());
        playlist.setDescription(item.getDescription());
        playlist.setPlaylistUrl(item.getExternalUrls().getSpotify());
        playlist.setTotalSongs(item.getTracks().getTotal());
        playlist.setProvider("Spotify");
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            playlist.setImageUrl(item.getImages().get(0).getUrl());
        }
        playlist.setSongs(fetchSongs(item.getId(), bearerToken));
        return playlist;
    }

    // Fetches songs for a playlist
    private List<Song> fetchSongs(String playlistId, String bearerToken) {
        String endpoint = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";
        ParameterizedTypeReference<SpotifySongsResponseDTO> responseType = new ParameterizedTypeReference<>() {};
        SpotifySongsResponseDTO tracksResponse = handleSpotifyApiCall(bearerToken, endpoint, HttpMethod.GET, responseType);

        return tracksResponse.getItems().stream()
                .map(trackItem -> {
                    Song song = new Song();
                    song.setId(trackItem.getTrack().getId());
                    song.setTitle(trackItem.getTrack().getName());
                    song.setArtist(trackItem.getTrack().getArtists().stream().map(SpotifySongsResponseDTO.TrackItem.Track.Artist::getName).collect(Collectors.joining(", ")));

                    Album album = new Album();
                    Optional<SpotifySongsResponseDTO.TrackItem.Track.Album> albumOptional = Optional.ofNullable(trackItem.getTrack().getAlbum());
                    albumOptional.ifPresent(a -> album.setCoverUrl(a.getImages().stream().findFirst().map(SpotifySongsResponseDTO.TrackItem.Track.Album.Image::getUrl).orElse(null)));
                    song.setAlbum(albumOptional.isPresent() ? album : null);

                    return song;
                })
                .collect(Collectors.toList());
    }

    // Save playlists to user
    private void savePlaylistsToUser(User user, List<Playlist> playlists) {
        user.setPlaylists(playlists);
        user.setLastPlaylistSync(new Date());
        userRepository.save(user);
    }

    // Get Access Token
    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to fetch access token: " + response.getStatusCode());
        }

        return response.getBody().get("access_token").toString();
    }

    // Get Top Songs
    public List<TopTrack.Track> getTopSongs() {
        TopTrack topTrack = topTrackRepo.findById("top_tracks").orElse(null);
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime todayMidnightUTC = nowUTC.toLocalDate().atStartOfDay(ZoneOffset.UTC);

        if (topTrack == null || topTrack.getLastUpdated().isBefore(todayMidnightUTC.toLocalDateTime())) {
            List<TopTrack.Track> topTracks = fetchTopTracksFromSpotify();
            if (topTrack == null) {
                topTrack = new TopTrack();
                topTrack.setId("top_tracks");
            }
            topTrack.setTracks(topTracks);
            topTrack.setLastUpdated(LocalDateTime.now(ZoneOffset.UTC));
            topTrackRepo.save(topTrack);
        }

        return topTrack.getTracks();
    }

    // Fetch top tracks from Spotify
    private List<TopTrack.Track> fetchTopTracksFromSpotify() {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                PLAYLIST_URL,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to fetch top tracks: " + response.getStatusCode());
        }

        List<TopTrack.Track> topTracks = new ArrayList<>();
        List<Map<String, Object>> items = objectMapper.convertValue(response.getBody().get("items"), new TypeReference<>() {
        });

        for (int i = 0; i < 50 && i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            Map<String, Object> track = objectMapper.convertValue(item.get("track"), new TypeReference<>() {
            });
            Map<String, Object> album = objectMapper.convertValue(track.get("album"), new TypeReference<>() {
            });
            List<Map<String, Object>> images = objectMapper.convertValue(album.get("images"), new TypeReference<>() {
            });

            List<Map<String, Object>> artistList = objectMapper.convertValue(track.get("artists"), new TypeReference<>() {
            });
            StringBuilder artistNames = new StringBuilder();
            for (Map<String, Object> artist : artistList) {
                if (!artistNames.isEmpty()) {
                    artistNames.append(", ");
                }
                artistNames.append(artist.get("name").toString());
            }

            TopTrack.Track songDetails = new TopTrack.Track();
            songDetails.setName(track.get("name").toString());
            songDetails.setArtist(artistNames.toString());
            if (!images.isEmpty()) {
                songDetails.setImage(images.get(0).get("url").toString());
            }
            topTracks.add(songDetails);
        }

        return topTracks;
    }
}
