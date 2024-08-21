package com.melodify.Melodify.Config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentConfig {

    private static final Dotenv dotenv = loadDotenv();

    // Environment variables are loaded from .env if available, otherwise from system environment
    public static final String SPOTIFY_CLIENT_ID = getEnv("SPOTIFY_CLIENT_ID");
    public static final String SPOTIFY_CLIENT_SECRET = getEnv("SPOTIFY_CLIENT_SECRET");
    public static final String SPOTIFY_REDIRECT_URI = getEnv("SPOTIFY_REDIRECT_URI");
    public static final String GENIUS_API_KEY = getEnv("GENIUS_API_KEY");
    public static final String LYRICS_API_URL = getEnv("LYRICS_API_URL");
    public static final String SENTIMENT_API_KEY = getEnv("SENTIMENT_API_KEY");
    public static final String SENTIMENT_PROMPT = decodePrompt(getEnv("SENTIMENT_PROMPT"));
    public static final String JWT_KEY = getEnv("JWT_KEY");

    static {
        // Load environment variables from the .env file (if available) as system properties
        if (dotenv != null) {
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        }
    }

    private static Dotenv loadDotenv() {
        try {
            return Dotenv.configure()
                    .filename(".env")
                    .load();
        } catch (Exception e) {
            // Log a message indicating that the .env file was not found or couldn't be loaded
            System.out.println("Could not load .env file, falling back to system environment variables.");
            return null;
        }
    }

    private static String getEnv(String key) {
        if (dotenv != null && dotenv.get(key) != null) {
            return dotenv.get(key);
        }
        return System.getenv(key);
    }

    private static String decodePrompt(String encodedPrompt) {
        return encodedPrompt != null ? encodedPrompt.replace("\\n", "\n") : null;
    }
}
