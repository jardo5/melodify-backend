package com.melodify.Melodify.Services;

import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> signUp(String username, String email, String password) {
        // Validate password
        if (!isValidPassword(password)) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Password must be at least 8 characters long and contain an uppercase letter, a lowercase letter, a number, and a special character."),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validate email
        if (!isValidEmail(email)) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Invalid email format."),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Check if username or email already exists
        if (userRepo.findByUsername(username).isPresent() || userRepo.findByEmail(email).isPresent()) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Username or email already exists."),
                    HttpStatus.CONFLICT
            );
        }

        // Create and save the new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("user");

        User savedUser = userRepo.save(newUser);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // Password validation method
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChar = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

    // Email validation method
    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }

    public ResponseEntity<?> login(String usernameOrEmail, String password) {
        Optional<User> optionalUser = userRepo.findByUsername(usernameOrEmail);
        if (optionalUser.isEmpty()) {
            optionalUser = userRepo.findByEmail(usernameOrEmail);
        }

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return new ResponseEntity<>(user, HttpStatus.OK);
            }
        }

        // Returning JSON response with an error message
        return new ResponseEntity<>(
                Collections.singletonMap("error", "Invalid username/email or password"),
                HttpStatus.UNAUTHORIZED
        );
    }
}
