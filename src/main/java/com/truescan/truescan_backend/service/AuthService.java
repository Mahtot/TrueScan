package com.truescan.truescan_backend.service;

import com.truescan.truescan_backend.dto.ApiResponse;
import com.truescan.truescan_backend.dto.RegisterRequest;
import com.truescan.truescan_backend.exception.InvalidCredentialsException;
import com.truescan.truescan_backend.model.User;
import com.truescan.truescan_backend.repository.UserRepository;
import com.truescan.truescan_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

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


    public ResponseEntity<ApiResponse<String>> register( RegisterRequest request) {
        System.out.println("Reached register endpoint");

        if (request.getRole().equalsIgnoreCase("ADMIN")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Registration as ADMIN is not allowed.", null));
        }

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            User existing = existingUser.get();

            if (existing.isEnabled()) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false, "A user with this email already exists.", null));
            }

            if (existing.getOtpExpiry() != null && existing.getOtpExpiry().isBefore(LocalDateTime.now())) {
                existing.setOtpCode(generateOtpCode());
                existing.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
                emailService.sendOtpEmail(existing.getEmail(), existing.getOtpCode());
                userRepository.save(existing);

                return ResponseEntity.ok(
                        new ApiResponse<>(true, "OTP re-sent to your email.", null)
                );
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "OTP still valid. Please check your email.", null));
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

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "OTP sent to email. Please verify.", null));
    }


    public ResponseEntity<ApiResponse<String>> login( String email, String password) {
        System.out.println("Reached login endpoint");

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidCredentialsException("User not found"));

            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Please verify your email first.", null));
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid email or password.", null));
            }

            String jwtToken = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", jwtToken));

        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Login failed due to an unexpected error", null));
        }
    }

    private String generateOtpCode() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    public  ResponseEntity<ApiResponse<String>>  verifyOtp(String email, String otpCode) {
 try{
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if(user.getOtpCode() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "OTP expired. Please register again.", null));
        }

        if (!user.getOtpCode().equals(otpCode)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Invalid OTP code.", null));
        }

        user.setEnabled(true);
        user.setOtpCode(null); // clear OTP
        user.setOtpExpiry(null);
        userRepository.save(user);
//       Generate token after successful verification
        String jwt = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new ApiResponse<>(true, "OTP verified successfully", jwt));

    } catch (InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Something went wrong while verifying OTP", null));
    }    }

}
