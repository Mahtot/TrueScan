package com.truescan.truescan_backend.controller;

import com.truescan.truescan_backend.dto.QRCodeVerificationRequest;
import com.truescan.truescan_backend.dto.QRCodeVerificationResponse;
import com.truescan.truescan_backend.model.ErrorResponse;
import com.truescan.truescan_backend.model.Product;
import com.truescan.truescan_backend.service.ProductService;
import com.truescan.truescan_backend.util.QRCodeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // Register Product
    @PreAuthorize("hasAuthority('Manufacturer')")
    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        try {
            Product created = service.addProduct(product);

            // Generate QR Code using the product's serial number
            String qrBase64 = qrCodeHelper.generateQRCodeBase64(created.getSerialNumber(), 300, 300);

            // Construct response with product and QR
            Map<String, Object> response = new HashMap<>();
            response.put("product", created);
            response.put("qrCode", qrBase64);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("DUPLICATE_SERIAL", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("QR_GENERATION_FAILED", "Failed to generate QR code.")
            );
        }
    }


    //  Get product
    @PreAuthorize("hasAuthority('Manufacturer')")
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

    @GetMapping
    public ResponseEntity<?> getProductsForUser() {
        List<Product> products = service.getProductsForAuthenticatedManufacturer();

        if (products.isEmpty()) {
            ErrorResponse error = new ErrorResponse("NO_PRODUCTS_FOUND", "No products found for your account.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        return ResponseEntity.ok(products);
    }


//    Deleting a product
    @PreAuthorize("hasAuthority('Manufacturer')")
    @DeleteMapping("/{serial}")
    public ResponseEntity<Object> deleteProduct(@PathVariable String serial) {
        boolean deleted = service.deleteProduct(serial);

        if(deleted) {
            return ResponseEntity.ok().body("Product with serial number " + serial + " deleted successfully.");
        } else {
            ErrorResponse errorResponse = new ErrorResponse("PRODUCT_NOT_FOUND", "Product with serial number " + serial + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

//Update a product
@PreAuthorize("hasAuthority('Manufacturer')")
@PutMapping("/{serial}")
public ResponseEntity<Object> updateProduct(@PathVariable String serial, @RequestBody Product updatedProduct) {
    Optional<Product> updated = service.updateProduct(serial, updatedProduct);

    if (updated.isPresent()) {
        return ResponseEntity.ok(updated.get());
    } else {
        ErrorResponse errorResponse = new ErrorResponse("PRODUCT_NOT_FOUND", "Product with serial number " + serial + " not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}


// Generate a QR Code

@Autowired   //create an instance of the QRCodeHelper class
private QRCodeHelper qrCodeHelper;
    @PreAuthorize("hasAuthority('Manufacturer')")
    @GetMapping("/{serial}/qrcode")

    public ResponseEntity<?> generateQRCode(@PathVariable String serial) {
        Optional<Product> product = service.checkProduct(serial);

        if (product.isEmpty()) {
            ErrorResponse error = new ErrorResponse("PRODUCT_NOT_FOUND", "No product with serial: " + serial);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        try {
            String qrBase64 = qrCodeHelper.generateQRCodeBase64(serial, 300, 300);
            return ResponseEntity.ok().body(qrBase64);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("QR_GENERATION_FAILED", "Could not generate QR code.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyQRCode(@RequestBody QRCodeVerificationRequest request) {
        if (request.getSerial() == null || request.getSerial().isEmpty() ||
                request.getTimestamp() == null || request.getTimestamp().isEmpty() ||
                request.getSignature() == null || request.getSignature().isEmpty()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new QRCodeVerificationResponse(
                            false,
                            "Error: Missing QR code data. Please provide serial, timestamp, and signature.",
                            null, null, null, null
                    )
            );
        }

        Optional<Product> product = service.verifyProductFromQRCode(
                request.getSerial(),
                request.getTimestamp(),
                request.getSignature()
        );

        if (product.isPresent()) {
            Product p = product.get();
            return ResponseEntity.ok(new QRCodeVerificationResponse(
                    true,
                    "Product is authentic.",
                    p.getName(),
                    p.getSerialNumber(),
                    p.getManufacturerCompany(),
                    p.getManufacturerEmail()
            ));
        } else {
            // Step 4: Return unauthorized response if product is not found or signature is invalid
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new QRCodeVerificationResponse(
                            false,
                            "Invalid or tampered QR code. Product could not be verified.",
                            null,
                            null,
                            null,
                            null
                    )
            );
        }
    }

}
