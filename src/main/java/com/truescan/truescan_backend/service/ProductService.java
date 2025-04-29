package com.truescan.truescan_backend.service;
import com.truescan.truescan_backend.model.Product;

import com.truescan.truescan_backend.repository.ProductRepository;
import com.truescan.truescan_backend.util.QRCodeHelper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public List<Product> getProductsForAuthenticatedManufacturer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repo.findAllByManufacturerEmail(email);    }

//    Delete a product
    public boolean deleteProduct(String serialNumber) {
        Optional<Product> product = repo.findBySerialNumber(serialNumber);

        if(product.isPresent()) {
            repo.delete(product.get());
            return true;
        } else {
            return false;
        }

    }


//    Update product
public Optional<Product> updateProduct(String serialNumber, Product updatedProduct) {
    Optional<Product> existing = repo.findBySerialNumber(serialNumber);
    if (existing.isPresent()) {
        Product product = existing.get();
        product.setName(updatedProduct.getName());
        product.setAuthentic(updatedProduct.isAuthentic());
        product.setManufacturerEmail(updatedProduct.getManufacturerEmail());
        product.setManufacturerCompany(updatedProduct.getManufacturerCompany());

        return Optional.of(repo.save(product));
    }
    return Optional.empty();
}

//    verifies incoming qrcode from frontend
@Autowired
    QRCodeHelper qrCodeHelper;
public Optional<Product> verifyProductFromQRCode(String serial, String timestamp, String signature) {
        Optional<Product> product = checkProduct(serial);

        if (product.isEmpty()) return Optional.empty();

        boolean isValid = qrCodeHelper.isSignatureValid(serial, timestamp, signature);
        return isValid ? product : Optional.empty();
    }

}
