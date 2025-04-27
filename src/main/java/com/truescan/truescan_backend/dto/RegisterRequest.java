package com.truescan.truescan_backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String companyName;
    private String role;
}
