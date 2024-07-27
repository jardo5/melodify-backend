package com.melodify.Melodify.Services.SongService;

import com.melodify.Melodify.Models.Song;
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

    @Autowired
    public SongService(GeniusService geniusService, LyricsService lyricsService, SentimentAnalysisService sentimentAnalysisService) {
        this.geniusService = geniusService;
        this.lyricsService = lyricsService;
        this.sentimentAnalysisService = sentimentAnalysisService;
    }

    public List<Map<String, String>> searchGenius(String query) {
        return geniusService.search(query);
    }

    public Song getSongDetails(String songId) {
        // Fetch song details from Genius
        Song song = geniusService.getSongDetails(songId);

        // Fetch lyrics for the song with timeout
        String lyrics = lyricsService.fetchLyricsWithTimeout(song.getArtist(), song.getTitle(), 10, TimeUnit.SECONDS);
        if (lyrics.equals("Lyrics request timed out") || lyrics.equals("Lyrics not found")) {
            song.setLyrics("Lyrics Not Found Refer To Genius: " + song.getGeniusUrl());
        } else {
            song.setLyrics(lyrics);
            String sentiment = sentimentAnalysisService.analyzeSentimentWithTimeout(lyrics, 10, TimeUnit.SECONDS);
            song.setSentiment(sentiment);
        }

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
