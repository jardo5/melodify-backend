package com.melodify.Melodify.Config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .filename(".env")
            .load();
    
    public static final String SPOTIFY_CLIENT_ID = dotenv.get("SPOTIFY_CLIENT_ID");
    public static final String SPOTIFY_CLIENT_SECRET = dotenv.get("SPOTIFY_CLIENT_SECRET");
    public static final String GENIUS_API_KEY = dotenv.get("GENIUS_API_KEY");
    public static final String LYRICS_API_URL = dotenv.get("LYRICS_API_URL");
    public static final String SENTIMENT_API_KEY = dotenv.get("SENTIMENT_API_KEY");
    public static final String SENTIMENT_PROMPT = decodePrompt(dotenv.get("SENTIMENT_PROMPT"));
    public static final String JWT_KEY = dotenv.get("JWT_KEY");
    
    public static final String DEV_FRONTEND_URL = dotenv.get("DEV_FRONTEND_URL");
    public static final String PROD_FRONTEND_URL = dotenv.get("PROD_FRONTEND_URL");

    private static String decodePrompt(String encodedPrompt) {
        return encodedPrompt.replace("\\n", "\n");
    }

    static {
        // Load environment variables as system properties
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
}
