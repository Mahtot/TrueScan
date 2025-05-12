package com.truescan.truescan_backend.controller;

import com.truescan.truescan_backend.dto.ApiResponse;
import com.truescan.truescan_backend.dto.RegisterRequest;
import com.truescan.truescan_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
       return authService.register(request);
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody Map<String, String> request) {
        return authService.login(request.get("email"), request.get("password"));

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        return authService.verifyOtp(email, otp);
    }

















}
