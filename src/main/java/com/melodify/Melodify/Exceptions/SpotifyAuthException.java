package com.melodify.Melodify.Exceptions;

public class SpotifyAuthException extends RuntimeException {
    public SpotifyAuthException(String message) {
        super(message);
    }

    public SpotifyAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
