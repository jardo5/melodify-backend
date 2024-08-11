package com.melodify.Melodify.Services;

import com.melodify.Melodify.DTOs.ArtistWithSongsDTO;
import com.melodify.Melodify.DTOs.SongDTO;
import com.melodify.Melodify.Models.Artist;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Repositories.ArtistRepo;
import com.melodify.Melodify.Repositories.SongRepo;
import com.melodify.Melodify.Services.ArtistService.ArtistService;
import com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService.SpotifyApiService;
import com.melodify.Melodify.Services.SongService.SongService;
import com.melodify.Melodify.Services.PlaylistService.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sound.midi.VoiceStatus;
import java.util.*;
import java.util.logging.Logger;

@Service
public class SongCrawlerService {

    private final SpotifyApiService spotifyApiService;
    private final GeniusService geniusService;
    private final SongService songService;
    private final SongRepo songRepo;
    private final ArtistRepo artistRepo;
    private List<Song> fetchedSongs;

    private static final Logger logger = Logger.getLogger(SongCrawlerService.class.getName());

    @Autowired
    public SongCrawlerService(SpotifyApiService spotifyApiService, GeniusService geniusService, SongService songService, SongRepo songRepo, ArtistRepo artistRepo) {
        this.spotifyApiService = spotifyApiService;
        this.geniusService = geniusService;
        this.songService = songService;
        this.songRepo = songRepo;
        this.artistRepo = artistRepo;
        this.fetchedSongs = new ArrayList<>();
    }
    
    /*
    Please DO NOT abuse this Service! 
    This service is used to fetch songs from Spotify and convert them to Genius songs via Song name and Arist name.
    Your Spotify and GeniusAPI keys will be rate limited/banned if you make too many requests. 
    */
    

    public void crawlSongsFromSpotify(Map<String, String> playlistIds, String bearerToken) {
        Set<String> uniqueSongKeys = new HashSet<>();

        logger.info("Starting to fetch songs from playlists.");

        for (String playlistId : playlistIds.keySet()) {
            logger.info("Fetching songs from playlist ID: " + playlistId);

            while (true) {
                try {
                    List<Song> songs = spotifyApiService.fetchSongs(playlistId, bearerToken);

                    for (Song song : songs) {
                        if (song != null && song.getTitle() != null && song.getArtist() != null) {
                            String songKey = song.getTitle() + "-" + song.getArtist();
                            if (!uniqueSongKeys.contains(songKey)) {
                                uniqueSongKeys.add(songKey);
                                fetchedSongs.add(song);
                                logger.info("Added song: " + song.getTitle() + " by " + song.getArtist());
                            } else {
                                logger.info("Duplicate song found, skipping: " + song.getTitle() + " by " + song.getArtist());
                            }
                        } else {
                            logger.warning("Encountered a null track or incomplete track information.");
                        }
                    }
                    break; // Exit the loop if successful

                } catch (Exception e) {
                    if (e.getMessage().contains("429")) {
                        logger.warning("Hit Spotify rate limit. Pausing for 30 minutes.");
                        try {
                            Thread.sleep(1800000); // Sleep for 30 minutes
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            logger.warning("Thread was interrupted during rate limit pause: " + interruptedException.getMessage());
                        }
                    } else {
                        logger.severe("Error fetching songs from Spotify: " + e.getMessage());
                        break; // Exit the loop on other errors
                    }
                }

                // Introduce a delay to avoid hitting API rate limits
                try {
                    Thread.sleep(500); // Sleep for 0.5 seconds between each song fetch
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warning("Thread was interrupted: " + e.getMessage());
                }
            }
        }

        logger.info("Finished fetching songs. Total unique songs fetched: " + fetchedSongs.size());
    }

    public void convertToGeniusSongsAndPersist() {
        logger.info("Starting to convert and persist songs.");

        for (Song song : fetchedSongs) {
            logger.info("Processing song: " + song.getTitle() + " by " + song.getArtist());

            while (true) {
                try {
                    if (!songRepo.existsById(song.getId())) {
                        logger.info("Song not found in database, searching on Genius: " + song.getTitle() + " by " + song.getArtist());

                        List<Map<String, String>> results = geniusService.search(song.getTitle() + " " + song.getArtist());
                        if (!results.isEmpty()) {
                            String songId = results.get(0).get("id");
                            logger.info("Genius song found: ID = " + songId);

                            Song fetchedSong = songService.getSongDetails(songId);
                            if (fetchedSong == null) {
                                logger.warning("Could not fetch song details from Genius. Skipping.");
                                break;
                            }

                            logger.info("Fetched song details from Genius: " + fetchedSong.getFullTitle());

                            if (fetchedSong.getPrimaryArtist() != null) {
                                Artist artistDetails = geniusService.getArtistDetails(fetchedSong.getPrimaryArtist().getId());
                                if (artistDetails == null) {
                                    logger.warning("Could not fetch artist details from Genius. Skipping.");
                                    break;
                                }

                                logger.info("Fetched artist details: " + artistDetails.getName());

                                if (!artistRepo.existsById(artistDetails.getId())) {
                                    logger.info("Artist not found in database, saving artist: " + artistDetails.getName());
                                    artistRepo.save(artistDetails);
                                } else {
                                    logger.info("Artist already exists in database, skipping save: " + artistDetails.getName());
                                }

                                fetchedSong.setPrimaryArtist(artistDetails);
                            }

                            logger.info("Saving song to database: " + fetchedSong.getFullTitle());
                            songRepo.save(fetchedSong);

                        } else {
                            logger.info("No results found on Genius for: " + song.getTitle() + " by " + song.getArtist());
                        }
                    } else {
                        logger.info("Song already exists in the database, skipping: " + song.getTitle() + " by " + song.getArtist());
                    }
                    break; // Exit the loop if successful

                } catch (Exception e) {
                    if (e.getMessage().contains("429")) {
                        logger.warning("Hit Genius rate limit. Pausing for 30 minutes.");
                        try {
                            Thread.sleep(1800000); // Sleep for 30 minutes
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            logger.warning("Thread was interrupted during rate limit pause: " + interruptedException.getMessage());
                        }
                    } else {
                        logger.severe("Error processing song from Genius: " + e.getMessage());
                        break; // Exit the loop on other errors
                    }
                }

                // Introduce a delay between processing each song
                try {
                    Thread.sleep(500); // Sleep for 0.5 seconds between each Genius API call
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warning("Thread was interrupted: " + e.getMessage());
                }
            }
        }

        logger.info("Finished converting and persisting songs.");
        fetchedSongs.clear();
    }
    

    public List<Song> fetchAndConvertSongsForTesting(Map<String, String> playlistIds, String bearerToken) {
        Set<String> uniqueSongKeys = new HashSet<>();
        List<Song> testFetchedSongs = new ArrayList<>();
        List<Song> songDetailsList = new ArrayList<>();

        // Limit to processing only 1 playlist
        String playlistId = playlistIds.keySet().iterator().next();

        logger.info("Fetching songs from playlist ID: " + playlistId);
        List<Song> songs = spotifyApiService.fetchSongs(playlistId, bearerToken);

        // Add only the first 10 unique songs to the testFetchedSongs list
        for (Song song : songs) {
            if (testFetchedSongs.size() >= 10) {
                break;
            }
            if (song != null && song.getTitle() != null && song.getArtist() != null) {
                String songKey = song.getTitle() + "-" + song.getArtist(); // Use a combination of title and artist as a unique key
                if (uniqueSongKeys.add(songKey)) { // Add returns false if the item was already in the set
                    testFetchedSongs.add(song);
                }
            } else {
                logger.warning("Encountered a null track or track information in playlist ID: " + playlistId);
            }
        }

        logger.info("Test fetching complete. Total unique songs fetched: " + testFetchedSongs.size());

        // Convert fetched songs using Genius API
        for (Song song : new ArrayList<>(testFetchedSongs)) { // Use a copy of the list to avoid concurrent modification
            logger.info("Searching for song on Genius: " + song.getTitle() + " by artist: " + song.getArtist());
            List<Map<String, String>> results = geniusService.search(song.getTitle() + " " + song.getArtist());

            if (!results.isEmpty()) {
                String songId = results.get(0).get("id");
                Song fetchedSong = songService.getSongDetails(songId);

                // Fetch full artist details
                Artist artistDetails = geniusService.getArtistDetails(fetchedSong.getPrimaryArtist().getId());
                List<SongDTO> topSongs = geniusService.getArtistTopSongs(fetchedSong.getPrimaryArtist().getId(), 10);
                artistDetails.setTopSongs(topSongs); // Set the top songs for the artist

                // Populate the primary artist with detailed information
                fetchedSong.setPrimaryArtist(artistDetails);

                songDetailsList.add(fetchedSong);
            }

            try {
                // Introduce a delay to avoid hitting API rate limits
                Thread.sleep(500); // Sleep for 0.5 seconds between each Genius API call
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning("Thread was interrupted: " + e.getMessage());
            }
        }

        return songDetailsList;
    } //TODO DELETE THIS METHOD

}

