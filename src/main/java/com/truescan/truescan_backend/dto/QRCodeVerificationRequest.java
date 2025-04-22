package com.truescan.truescan_backend.dto;

import lombok.Data;

@Data
public class QRCodeVerificationRequest {
    private String serial;
    private String timestamp;
    private String signature;
}