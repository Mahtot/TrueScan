package com.truescan.truescan_backend.controller;

import com.truescan.truescan_backend.model.User;
import com.truescan.truescan_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAuthority('Manufacturer')")
@RequestMapping("/manufacturer")

    public class ManufacturerController {
        private final UserService userService;

        public ManufacturerController(UserService userService) {
            this.userService = userService;
        }

        private String getAuthenticatedUserEmail() {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else {
                return principal.toString();
            }
        }

        // GET profile
        @GetMapping("/profile")
        public ResponseEntity<?> getProfile() {
            String email = getAuthenticatedUserEmail();
            return userService.getManufacturerProfile(email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        // PUT update profile
        @PutMapping("/profile")
        public ResponseEntity<?> updateProfile(@RequestBody User updatedUser) {
            String email = getAuthenticatedUserEmail();
            return userService.updateManufacturerProfile(email, updatedUser)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        // DELETE profile
        @DeleteMapping("/profile")
        public ResponseEntity<?> deleteProfile() {
            String email = getAuthenticatedUserEmail();
            boolean deleted = userService.deleteManufacturerProfile(email);
            return deleted ? ResponseEntity.ok("Profile deleted") : ResponseEntity.notFound().build();
        }
    }

