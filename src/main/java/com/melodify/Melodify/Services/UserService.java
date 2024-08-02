package com.melodify.Melodify.Services;

import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.UserRepo;
import com.melodify.Melodify.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<?> signUp(String username, String email, String password) {
        if (!isValidPassword(password)) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Password must be at least 8 characters long and contain an uppercase letter, a lowercase letter, a number, and a special character."),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!isValidEmail(email)) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Invalid email format."),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (userRepo.findByUsername(username).isPresent() || userRepo.findByEmail(email).isPresent()) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Username or email already exists."),
                    HttpStatus.CONFLICT
            );
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("USER");
        newUser.setConnectedAccounts(new ArrayList<>());
        newUser.setPlaylists(new ArrayList<>());
        newUser.setLastPlaylistSync(null);
        newUser.setDislikedSongs(new ArrayList<>());
        newUser.setLikedSongs(new ArrayList<>());
        

        User savedUser = userRepo.save(newUser);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

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
                String token = jwtUtil.generateToken(user.getUsername());
                return new ResponseEntity<>(Collections.singletonMap("token", token), HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(
                Collections.singletonMap("error", "Invalid username/email or password"),
                HttpStatus.UNAUTHORIZED
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    public ResponseEntity<?> getUserInfo(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim(); // Remove 'Bearer ' prefix and trim any whitespace
        } else {
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtil.extractUsername(token);
        Optional<User> userOptional = userRepo.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Removes unnecessary fields from the response
            User userInfo = new User();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setRole(user.getRole());
            userInfo.setConnectedAccounts(user.getConnectedAccounts());

            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

}
