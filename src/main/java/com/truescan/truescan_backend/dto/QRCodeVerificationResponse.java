package com.truescan.truescan_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QRCodeVerificationResponse {
    private boolean isValid;
    private String message;
    private String productName;
    private String serialNumber;
    private String manufacturerCompany;
    private String manufacturerEmail;

}
