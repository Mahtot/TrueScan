package com.truescan.truescan_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")

public class Product {
    @Id
    private String productId;
    private String name;
    private String serialNumber;
    private String manufacturerEmail;
    private boolean isAuthentic;
    private LocalDateTime registeredAt;
}

