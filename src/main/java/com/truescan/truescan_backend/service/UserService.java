package com.truescan.truescan_backend.service;

import com.truescan.truescan_backend.model.User;
import com.truescan.truescan_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get user by email
    public Optional<User> getManufacturerProfile(String email) {
        return userRepository.findByEmail(email);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> updateManufacturerProfile(String email, User updatedUser) {
        return userRepository.findByEmail(email).map(existingUser -> {
            // Update company name if provided
            if (updatedUser.getCompanyName() != null) {
                existingUser.setCompanyName(updatedUser.getCompanyName());
            }

            // Update email if provided
            if (updatedUser.getEmail() != null) {
                existingUser.setEmail(updatedUser.getEmail());
            }

            // Update password if provided
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                String encodedPassword = passwordEncoder.encode(updatedUser.getPassword());
                existingUser.setPassword(encodedPassword);
            }

            return userRepository.save(existingUser);
        });
    }


    // Delete profile
    public boolean deleteManufacturerProfile(String email) {
        return userRepository.findByEmail(email).map(user -> {
            userRepository.delete(user);
            return true;
        }).orElse(false);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findAllByRole(role);
    }

    public boolean deleteUserByEmail(String email) {
        return userRepository.findByEmail(email).map(user -> {
            userRepository.delete(user);
            return true;
        }).orElse(false);
    }

}
