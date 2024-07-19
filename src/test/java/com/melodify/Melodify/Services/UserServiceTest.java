package com.melodify.Melodify.Services;

import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignUp() {
        String username = "testUser";
        String email = "test@example.com";
        String password = "Test@1234";

        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        ResponseEntity<?> response = userService.signUp(username, email, password);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testLoginWithUsername() {
        String username = "testUser";
        String password = "Test@1234";
        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        ResponseEntity<?> response = userService.login(username, password);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testLoginWithEmail() {
        String email = "test@example.com";
        String password = "Test@1234";
        User user = new User();
        user.setEmail(email);
        user.setPassword("encodedPassword");

        when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        ResponseEntity<?> response = userService.login(email, password);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testLoadUserByUsername() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setRole("USER");

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));

        org.springframework.security.core.userdetails.UserDetails userDetails = userService.loadUserByUsername(username);
        assertEquals(username, userDetails.getUsername());
    }
}
