package com.truescan.truescan_backend.controller;

import com.truescan.truescan_backend.model.Product;
import com.truescan.truescan_backend.model.User;
import com.truescan.truescan_backend.service.ProductService;
import com.truescan.truescan_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/admin")
@RequiredArgsConstructor

public class AdminController {

    private final UserService userService;
    private final ProductService productService;

    @GetMapping("/manufacturers")
    public List<User> getAllManufacturers() {
        return userService.getUsersByRole("Manufacturer");
    }

    @DeleteMapping("/manufacturer/{email}")
    public ResponseEntity<?> deleteManufacturer(@PathVariable String email) {
        boolean deleted = userService.deleteUserByEmail(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");


        return deleted ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @DeleteMapping("/product/{serial}")
    public ResponseEntity<?> deleteProduct(@PathVariable String serial) {
        boolean deleted = productService.deleteProduct(serial);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");

        return deleted ? ResponseEntity.ok(response): ResponseEntity.status(HttpStatus.NOT_FOUND).body("PRODUCT NOT FOUND");
    }
}
