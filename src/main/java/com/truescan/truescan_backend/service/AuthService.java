package com.truescan.truescan_backend.service;

import com.truescan.truescan_backend.dto.RegisterRequest;
import com.truescan.truescan_backend.exception.InvalidCredentialsException;
import com.truescan.truescan_backend.model.User;
import com.truescan.truescan_backend.repository.UserRepository;
import com.truescan.truescan_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    //  Register a new user
    @Autowired
    private EmailService emailService;

    private LocalDateTime otpExpiry;

    public String register(RegisterRequest request) {
        System.out.println("Reached register endpoint");

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new InvalidCredentialsException("A user with this email already exists.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCompanyName(request.getCompanyName());
        user.setRole(request.getRole());

        // OTP
        String otpCode = generateOtpCode();
        user.setOtpCode(otpCode);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setEnabled(false);

        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otpCode);

        return "OTP sent to email. Please verify.";
    }


    //  Login
    public String login(String email, String password) {
        System.out.println("Reached login endpoint");

        User user = userRepository.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Please verify your email first.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return jwtUtil.generateToken(user);
    }

    private String generateOtpCode() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    public String verifyOtp(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (user.getOtpCode() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("OTP expired. Please register again.");
        }

        if (!user.getOtpCode().equals(otpCode)) {
            throw new InvalidCredentialsException("Invalid OTP code.");
        }

        user.setEnabled(true);
        user.setOtpCode(null); // clear OTP
        user.setOtpExpiry(null);
        userRepository.save(user);
//       Generate token after successful verification
        return jwtUtil.generateToken(user);
    }

}
