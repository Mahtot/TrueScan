package com.truescan.truescan_backend.controller;

import com.truescan.truescan_backend.model.ErrorResponse;
import com.truescan.truescan_backend.model.Product;
import com.truescan.truescan_backend.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // Register Product
    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        try {
            Product created = service.addProduct(product);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("DUPLICATE_SERIAL", e.getMessage()));
        }
    }

//  Get product
    @GetMapping("/{serial}")
    public ResponseEntity<Object> getProduct(@PathVariable String serial) {
        // Attempt to retrieve the product by serial number
        Optional<Product> product = service.checkProduct(serial);

        // If product is found, return it in the response body with 200 OK
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            // If product is not found, return 404 with error message
            ErrorResponse errorResponse = new ErrorResponse("PRODUCT_NOT_FOUND", "Product with serial number " + serial + " not found.");
            return ResponseEntity.status(404).body(errorResponse);
        }
    }

//    Get Products by email
    @GetMapping
    public ResponseEntity<?> getProductsByEmail(@RequestParam String email) {
        List<Product> products = service.getProductsByManufacturerEmail(email);

        if (products.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse("NO_PRODUCTS_FOUND", "No products found for email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        return ResponseEntity.ok(products);
    }




}
