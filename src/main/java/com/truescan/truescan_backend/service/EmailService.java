// EmailService.java

package com.truescan.truescan_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your TrueScan OTP Code");
        message.setText("Your OTP code is: " + otpCode + "\nIt will expire in 5 minutes.");
        mailSender.send(message);
    }
}
