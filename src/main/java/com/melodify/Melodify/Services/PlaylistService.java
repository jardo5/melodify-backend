package com.melodify.Melodify.Services.PlaylistService;

import com.melodify.Melodify.DTOs.ArtistWithSongsDTO;
import com.melodify.Melodify.Models.Playlist;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Repositories.ArtistRepo;
import com.melodify.Melodify.Repositories.SongRepo;
import com.melodify.Melodify.Services.ArtistService.ArtistService;
import com.melodify.Melodify.Services.GeniusService;
import com.melodify.Melodify.Services.SongService.SongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    private final GeniusService geniusService;
    private final SongService songService;
    private final ArtistService artistService;
    private final ArtistRepo artistRepo;
    private final SongRepo songRepo;

    @Autowired
    public PlaylistService(GeniusService geniusService, SongService songService, ArtistService artistService, ArtistRepo artistRepo, SongRepo songRepo) {
        this.geniusService = geniusService;
        this.songService = songService;
        this.artistService = artistService;
        this.artistRepo = artistRepo;
        this.songRepo = songRepo;
    }

    public List<Song> fetchAndPersistSongsFromNewPlaylist(Playlist playlist) {
        List<Song> newSongs = new ArrayList<>();
        logger.info("Processing new playlist: {}", playlist.getName());
        playlist.getSongs().forEach(song -> {
            logger.info("Searching Genius for song: {} by {}", song.getTitle(), song.getArtist());
            List<Map<String, String>> results = geniusService.search(song.getTitle() + " " + song.getArtist());
            if (!results.isEmpty()) {
                String songId = results.get(0).get("id");
                logger.info("Fetching details for song ID: {}", songId);
                Song fetchedSong = songService.getSongDetails(songId);
                if (fetchedSong.getPrimaryArtist() != null) {
                    ArtistWithSongsDTO artistWithSongs = artistService.getArtistWithTopSongs(fetchedSong.getPrimaryArtist().getId(), 10);
                    fetchedSong.setPrimaryArtist(artistWithSongs.getArtist());
                    artistRepo.save(artistWithSongs.getArtist()); // Save artist details
                }
                songRepo.save(fetchedSong); // Save song details
                newSongs.add(fetchedSong);
            } else {
                logger.warn("No search results found for song: {} by {}", song.getTitle(), song.getArtist());
            }
        });
        logger.info("Finished processing new playlist: {}", playlist.getName());
        return newSongs;
    }
}
