package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "Users")
public class User {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String username;
    
    private String password; // BCrypt hashed
    
    @Indexed(unique = true)
    private String email;
    
    private String role; //user or admin
    
    private List<ConnectedAccounts> connectedAccounts; // Reference to the ConnectedAccounts model
    
    @Data
    public static class ConnectedAccounts {
        private String provider;
        private String accessToken;
        private String refreshToken;
        private long expiresAt;
    }
}
