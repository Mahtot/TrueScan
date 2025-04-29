package com.truescan.truescan_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "users")
@Data // generates getters and setters
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String companyName;

//    Role could be "MANUFACTURER", "ADMIN"
    private String role;

    private boolean enabled = false;  // new users are disabled
    private String otpCode;           // 6-digit code we send to email
    private LocalDateTime otpExpiry;           // expiry time for the code

}
