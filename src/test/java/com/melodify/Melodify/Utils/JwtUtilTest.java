package com.melodify.Melodify.Utils;

import com.melodify.Melodify.Config.EnvironmentConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    public void testGenerateToken() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);

        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername);

        Date expirationDate = jwtUtil.extractExpiration(token);
        assertTrue(expirationDate.after(new Date()));

        assertTrue(jwtUtil.validateToken(token, username));
    }

    @Test
    public void testExtractUsername() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    public void testExtractExpiration() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);
        Date expirationDate = jwtUtil.extractExpiration(token);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    public void testExtractClaim() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);
        String extractedUsername = jwtUtil.extractClaim(token, Claims::getSubject);
        assertEquals(username, extractedUsername);
    }

    @Test
    public void testIsTokenExpired() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);

        // Token should not be expired immediately after creation
        assertFalse(jwtUtil.isTokenExpired(token));

        // Manually creating an expired token using the same key
        String expiredToken = Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)) // issued 24 hours ago
                .expiration(new Date(System.currentTimeMillis() - 1000)) // expired 1 second ago
                .signWith(jwtUtil.getKey()) // use the same key from JwtUtil
                .compact();

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtUtil.isTokenExpired(expiredToken);
        });
    }


    @Test
    public void testCreateToken() {
        String username = "testUser";
        Map<String, Object> claims = new HashMap<>();
        String token = jwtUtil.generateToken(username);
        assertNotNull(token);
    }

    @Test
    public void testValidateToken() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);
        assertTrue(jwtUtil.validateToken(token, username));
    }

    @Test
    public void testTokenNotExpired() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);

        // Check if the token is valid and not expired
        assertTrue(jwtUtil.validateToken(token, username));

        // Check if the token expiration date is in the future
        Date expirationDate = jwtUtil.extractExpiration(token);
        assertFalse(expirationDate.before(new Date()));
    }
}
