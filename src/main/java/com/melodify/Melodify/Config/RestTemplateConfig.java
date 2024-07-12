package com.melodify.Melodify.Config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .filename(".env")
            .load();
    public static final String SPOTIFY_CLIENT_ID = dotenv.get("SPOTIFY_CLIENT_ID");
    public static final String SPOTIFY_CLIENT_SECRET = dotenv.get("SPOTIFY_CLIENT_SECRET");
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
