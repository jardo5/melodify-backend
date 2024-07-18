package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.DTOs.LoginRequest;
import com.melodify.Melodify.DTOs.SignUpRequest;
import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
