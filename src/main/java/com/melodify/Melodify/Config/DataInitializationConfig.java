package com.melodify.Melodify.Config;

import com.melodify.Melodify.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setEmail("example@gmail.com");
            admin.setRole("ADMIN");
            List<ConnectedAccountsService> connectedAccounts = new ArrayList<>();
            admin.setConnectedAccounts(connectedAccounts);
            userRepo.save(admin);
        }
    } */
}
