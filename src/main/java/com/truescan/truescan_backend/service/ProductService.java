package com.truescan.truescan_backend.service;
import com.truescan.truescan_backend.model.Product;

import com.truescan.truescan_backend.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Product addProduct(Product product) {
        Optional<Product> existingProduct = repo.findBySerialNumber(product.getSerialNumber());

        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product with this serial number already exists.");
        }

        product.setRegisteredAt(LocalDateTime.now());
        product.setAuthentic(true);
        return repo.save(product);
    }


    public Optional<Product> checkProduct(String serialNumber) {
        return repo.findBySerialNumber(serialNumber);
    }

    public List<Product> getProductsByManufacturerEmail(String email) {
        return repo.findAllByManufacturerEmail(email);
    }


}
