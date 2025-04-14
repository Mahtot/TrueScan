package com.truescan.truescan_backend.repository;

import com.truescan.truescan_backend.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySerialNumber(String serialNumber);
    List<Product> findAllByManufacturerEmail(String manufacturerEmail);

}