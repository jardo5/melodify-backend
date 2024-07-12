package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "users")
public class User {
    private String id;
    private String username;
    private String password; // TODO: BCrypt
    private String email;
    private List<ConnectedAccounts> connectedAccounts;
    private List<String> favoriteSongs;
    
    @Data
    public static class ConnectedAccounts {
        private String provider;
        private String accessToken;
        private String refreshToken;
        private long expiresAt;
    }
}
