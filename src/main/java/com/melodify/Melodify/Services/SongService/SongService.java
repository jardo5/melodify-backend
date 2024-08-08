package com.melodify.Melodify.Services.SongService;

import com.melodify.Melodify.DTOs.ArtistWithSongsDTO;
import com.melodify.Melodify.Models.Artist;
import com.melodify.Melodify.Models.Playlist;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.ArtistRepo;
import com.melodify.Melodify.Repositories.SongRepo;
import com.melodify.Melodify.Repositories.UserRepo;
import com.melodify.Melodify.Services.ArtistService.ArtistService;
import com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService.SpotifyApiService;
import com.melodify.Melodify.Services.GeniusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SongService {

    //Logger
    private static final Logger logger = LoggerFactory.getLogger(SongService.class);

    private final GeniusService geniusService;
    private final LyricsService lyricsService;
    private final SentimentAnalysisService sentimentAnalysisService;

    private final SongRepo songRepo;
    private SpotifyApiService spotifyApiService;
    private final UserRepo userRepo;
    private final ArtistService artistService;
    private final ArtistRepo artistRepo;

    @Autowired
    public SongService(GeniusService geniusService, LyricsService lyricsService, SentimentAnalysisService sentimentAnalysisService, SongRepo songRepo, UserRepo userRepo, ArtistService artistService, ArtistRepo artistRepo) {
        this.geniusService = geniusService;
        this.lyricsService = lyricsService;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.songRepo = songRepo;
        this.userRepo = userRepo;
        this.artistService = artistService;
        this.artistRepo = artistRepo;
    }

    @Autowired
    public void setSpotifyApiService(@Lazy SpotifyApiService spotifyApiService) {
        this.spotifyApiService = spotifyApiService;
    }

    public List<Map<String, String>> searchGenius(String query) {
        return geniusService.search(query);
    }

    public Song getSongDetails(String songId) {
        // Check if the song is in the database
        Song song = songRepo.findById(songId).orElse(null);
        if (song != null) {
            return song;
        }

        song = geniusService.getSongDetails(songId);

        String lyrics = lyricsService.fetchLyricsWithTimeout(song.getArtist(), song.getTitle(), 10, TimeUnit.SECONDS);
        if ("Lyrics request timed out".equals(lyrics) || "Lyrics not found".equals(lyrics)) {
            song.setLyrics("Lyrics Not Found. Refer to Genius: " + song.getGeniusUrl());
        } else {
            song.setLyrics(lyrics);
            String sentiment = sentimentAnalysisService.analyzeSentimentWithTimeout(lyrics, 10, TimeUnit.SECONDS);
            song.setSentiment(sentiment);
        }

        songRepo.save(song);

        return song;
    }

    public List<Song> getSongDetailsBatch(List<String> songIds) {
        return songIds.stream()
                .map(this::getSongDetails)
                .toList();
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run once a day at midnight, every day Saves songs and artists from playlists and top charts
    public void scheduledFetchAndPersistSongs() {
        fetchAndPersistSongsFromPlaylistsAndTopCharts();
    }

    public List<Song> fetchAndPersistSongsFromPlaylistsAndTopCharts() {
        logger.info("Starting to fetch and persist songs and artists from playlists and top charts");
        List<Song> allSongs = spotifyApiService.getTopSongs().stream()
                .map(track -> {
                    logger.info("Searching Genius for track: {} by {}", track.getName(), track.getArtist());
                    List<Map<String, String>> results = geniusService.search(track.getName() + " " + track.getArtist());
                    if (!results.isEmpty()) {
                        String songId = results.get(0).get("id");
                        logger.info("Fetching details for song ID: {}", songId);
                        Song song = getSongDetails(songId);
                        if (song.getPrimaryArtist() != null) {
                            ArtistWithSongsDTO artistWithSongs = artistService.getArtistWithTopSongs(song.getPrimaryArtist().getId(), 10);
                            song.setPrimaryArtist(artistWithSongs.getArtist());
                            artistRepo.save(artistWithSongs.getArtist()); // Save artist details
                        }
                        songRepo.save(song); // Save song details
                        return song;
                    } else {
                        logger.warn("No search results found for track: {} by {}", track.getName(), track.getArtist());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<User> users = userRepo.findAll();
        logger.info("Total users found: {}", users.size());

        for (User user : users) {
            logger.info("Processing playlists for user: {}", user.getUsername());
            if (user.getPlaylists() == null) {
                logger.info("User {} has no playlists", user.getUsername());
                continue;
            }
            logger.info("User {} has {} playlists", user.getUsername(), user.getPlaylists().size());

            user.getPlaylists().forEach(playlist -> {
                logger.info("Processing playlist: {}", playlist.getName());
                playlist.getSongs().forEach(song -> {
                    logger.info("Searching Genius for song: {} by {}", song.getTitle(), song.getArtist());
                    List<Map<String, String>> results = geniusService.search(song.getTitle() + " " + song.getArtist());
                    if (!results.isEmpty()) {
                        String songId = results.get(0).get("id");
                        logger.info("Fetching details for song ID: {}", songId);
                        Song fetchedSong = getSongDetails(songId);
                        if (fetchedSong.getPrimaryArtist() != null) {
                            ArtistWithSongsDTO artistWithSongs = artistService.getArtistWithTopSongs(fetchedSong.getPrimaryArtist().getId(), 10);
                            fetchedSong.setPrimaryArtist(artistWithSongs.getArtist());
                            artistRepo.save(artistWithSongs.getArtist()); // Save artist details
                        }
                        songRepo.save(fetchedSong); // Save song details
                        allSongs.add(fetchedSong);
                    } else {
                        logger.warn("No search results found for song: {} by {}", song.getTitle(), song.getArtist());
                    }
                });
            });
        }

        logger.info("Finished fetching and persisting songs and artists from playlists and top charts");
        return allSongs;
    }

    //For Separate Endpoints
    public String fetchLyrics(String artist, String title) {
        return lyricsService.fetchLyrics(artist, title);
    }
    public String analyzeSentiment(String lyrics) {
        return sentimentAnalysisService.analyzeSentiment(lyrics);
    }
    //For Separate Endpoints
}
