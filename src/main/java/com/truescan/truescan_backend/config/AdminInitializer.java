package com.truescan.truescan_backend.config;

import com.truescan.truescan_backend.model.User;
import com.truescan.truescan_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@truescan.com";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setCompanyName("TrueScan");
            admin.setEnabled(true); // skip OTP for system admin

            userRepository.save(admin);
            System.out.println(" Admin user created.");
        } else {
            System.out.println("ℹ Admin already exists.");
        }
    }
}
