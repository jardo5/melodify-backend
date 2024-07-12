package com.melodify.Melodify.Services;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Service
public class GeniusService {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String GENIUS_API_URL = "https://api.genius.com/search?q=";
    private static final String GENIUS_API_KEY = dotenv.get("GENIUS_API_KEY");

    @Autowired
    private RestTemplate restTemplate;

    
}
