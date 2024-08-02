package com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService;

import com.melodify.Melodify.DTOs.SpotifyPlaylistsResponseDTO;
import com.melodify.Melodify.DTOs.SpotifySongsResponseDTO;
import com.melodify.Melodify.Models.Album;
import com.melodify.Melodify.Models.Playlist;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.UserRepo;
import com.melodify.Melodify.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpotifyApiService {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private SpotifyAuthService spotifyAuthService;

    @Autowired
    private JwtUtil jwtUtil;

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
        RestTemplate restTemplate = new RestTemplate();
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
        String endpoint = "https://api.spotify.com/v1/me/playlists";
        ParameterizedTypeReference<SpotifyPlaylistsResponseDTO> responseType = new ParameterizedTypeReference<>() {};
        SpotifyPlaylistsResponseDTO spotifyResponse = handleSpotifyApiCall(bearerToken, endpoint, HttpMethod.GET, responseType);
        return spotifyResponse.getItems().stream()
                .map(item -> mapToPlaylist(item, bearerToken))
                .collect(Collectors.toList());
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
}
