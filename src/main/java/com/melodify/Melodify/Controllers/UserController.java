package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.DTOs.LoginRequest;
import com.melodify.Melodify.DTOs.SignUpRequest;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Services.SongService.SongService;
import com.melodify.Melodify.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {
        return userService.signUp(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        return userService.getUserInfo(token);
    }

    @PostMapping("/{userId}/like")
    public ResponseEntity<?> likeSong(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        String songId = payload.get("songId");
        Map<String, String> response = userService.likeSong(userId, songId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/dislike")
    public ResponseEntity<?> dislikeSong(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        String songId = payload.get("songId");
        Map<String, String> response = userService.dislikeSong(userId, songId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/liked/{songId}")
    public ResponseEntity<?> removeLikedSong(@PathVariable String userId, @PathVariable String songId) {
        Map<String, String> response = userService.removeLikedSong(userId, songId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/disliked/{songId}")
    public ResponseEntity<?> removeDislikedSong(@PathVariable String userId, @PathVariable String songId) {
        Map<String, String> response = userService.removeDislikedSong(userId, songId);
        return ResponseEntity.ok(response);
    }
}