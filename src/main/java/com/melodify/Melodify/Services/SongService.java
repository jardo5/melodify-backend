package com.melodify.Melodify.Services;

import com.melodify.Melodify.Models.GeniusResponse;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Repositories.SongRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongService {

    @Autowired
    private SongRepo songRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String GENIUS_API_URL = "https://api.genius.com/search?q=";

    public List<Song> searchMusic(String query) {
        String url = GENIUS_API_URL + query;
        ResponseEntity<GeniusResponse> response = restTemplate.getForEntity(url, GeniusResponse.class);
        GeniusResponse geniusResponse = response.getBody();
        
        assert geniusResponse != null;
        List<Song> songs = geniusResponse.getResponse().getHits().stream().map(hit -> {
            Song song = new Song();
            song.setTitle(hit.getResult().getTitle());
            song.setArtist(hit.getResult().getPrimary_artist().getName());
            song.setFullTitle(hit.getResult().getFull_title());
            song.setGeniusUrl(hit.getResult().getUrl());
            song.setImageUrl(hit.getResult().getSong_art_image_url());
            song.setThumbnailUrl(hit.getResult().getSong_art_image_thumbnail_url());
            return song;
        }).collect(Collectors.toList());

        return songs;
    }

    public Song getMusicDetails(String id) {
        return songRepository.findById(id).orElse(null);
    }

    public Song saveMusic(Song song) {
        return songRepository.save(song);
    }
}
