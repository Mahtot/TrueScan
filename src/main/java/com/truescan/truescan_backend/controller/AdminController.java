package com.truescan.truescan_backend.controller;

import com.truescan.truescan_backend.model.Product;
import com.truescan.truescan_backend.model.User;
import com.truescan.truescan_backend.service.ProductService;
import com.truescan.truescan_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return deleted ? ResponseEntity.ok("Deleted.") : ResponseEntity.notFound().build();
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @DeleteMapping("/product/{serial}")
    public ResponseEntity<?> deleteProduct(@PathVariable String serial) {
        boolean deleted = productService.deleteProduct(serial);
        return deleted ? ResponseEntity.ok("Deleted.") : ResponseEntity.notFound().build();
    }
}
