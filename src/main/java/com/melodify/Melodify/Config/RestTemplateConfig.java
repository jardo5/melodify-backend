package com.melodify.Melodify.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .filename(".env")
            .load();
    
    //Spotify API credentials
    public static final String SPOTIFY_CLIENT_ID = dotenv.get("SPOTIFY_CLIENT_ID");
    public static final String SPOTIFY_CLIENT_SECRET = dotenv.get("SPOTIFY_CLIENT_SECRET");
    
    public static final String GENIUS_API_KEY = dotenv.get("GENIUS_API_KEY");

    public static final String LYRICS_API_URL = dotenv.get("LYRICS_API_URL");
    
    public static final String SENTIMENT_API_KEY = dotenv.get("SENTIMENT_API_KEY");
    public static final String SENTIMENT_PROMPT = decodePrompt(dotenv.get("SENTIMENT_PROMPT"));


    private static String decodePrompt(String encodedPrompt) {
        return encodedPrompt.replace("\\n", "\n");
    }

    static {
        // Load environment variables as system properties
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .additionalMessageConverters(customJackson2HttpMessageConverter())
                .build();
    }

    private HttpMessageConverter<Object> customJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN
        ));
        return converter;
    }
}
