package com.melodify.Melodify.Config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    private static final String GENIUS_API_KEY = Dotenv.load().get("GENIUS_API_KEY");

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().add("access_token", "Bearer " + GENIUS_API_KEY);
                    return execution.execute(request, body);
                })
                .build();
    }
}
