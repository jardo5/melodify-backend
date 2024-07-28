package com.melodify.Melodify.Services.SongService;

import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Repositories.SongRepo;
import com.melodify.Melodify.Services.GeniusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SongService {

    private final GeniusService geniusService;
    private final LyricsService lyricsService;
    private final SentimentAnalysisService sentimentAnalysisService;

    private final SongRepo songRepo;

    @Autowired
    public SongService(GeniusService geniusService, LyricsService lyricsService, SentimentAnalysisService sentimentAnalysisService, SongRepo songRepo) {
        this.geniusService = geniusService;
        this.lyricsService = lyricsService;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.songRepo = songRepo;
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
    
    //For Separate Endpoints
    public String fetchLyrics(String artist, String title) {
        return lyricsService.fetchLyrics(artist, title);
    }
    public String analyzeSentiment(String lyrics) {
        return sentimentAnalysisService.analyzeSentiment(lyrics);
    }
    //For Separate Endpoints
}
