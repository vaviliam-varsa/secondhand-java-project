package com.example.secondhandbackend.config;

import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.enums.Role;
import com.example.secondhandbackend.enums.UserStatus;
import com.example.secondhandbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures a default admin account always exists, so evaluators/testers
 * don't have to manually edit the database to promote a user to ADMIN.
 * Credentials are documented in the project README.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin@123";
    private static final String ADMIN_FULL_NAME = "System Administrator";
    private static final String ADMIN_PHONE_NUMBER = "09000000000";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(ADMIN_USERNAME)) {
            return;
        }

        User admin = new User();
        admin.setFullName(ADMIN_FULL_NAME);
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setPhoneNumber(ADMIN_PHONE_NUMBER);
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        userRepository.save(admin);
        log.info("Default admin account created (username: {})", ADMIN_USERNAME);
    }
}