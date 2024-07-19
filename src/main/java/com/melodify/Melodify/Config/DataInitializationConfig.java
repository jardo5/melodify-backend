package com.melodify.Melodify.Config;

import com.melodify.Melodify.Models.User;
import com.melodify.Melodify.Repositories.UserRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration // Use this class to create a user with the role of admin
public class DataInitializationConfig {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializationConfig(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /* @PostConstruct
    public void init() {
        if (userRepo.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("example_password"));
            admin.setEmail("example@example.com");
            admin.setRole("admin");
            userRepo.save(admin);
        }
    } */
}
