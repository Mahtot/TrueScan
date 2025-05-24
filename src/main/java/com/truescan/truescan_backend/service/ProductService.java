package com.truescan.truescan_backend.service;
import com.truescan.contracts.ProductVerifier;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.crypto.Credentials;
import org.web3j.tx.gas.DefaultGasProvider;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import com.truescan.truescan_backend.model.Product;

import com.truescan.truescan_backend.repository.ProductRepository;
import com.truescan.truescan_backend.util.QRCodeHelper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repo;
    private Web3j web3j;
    private ProductVerifier contract;

    @Value("${contract.productVerifierAddress}")
    private String CONTRACT_ADDRESS;

    @Value("${contract.private.key}")
    private String PRIVATE_KEY;

    @Autowired
    QRCodeHelper qrCodeHelper;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    private void init() {
        this.web3j = Web3j.build(new HttpService("https://sepolia.infura.io/v3/e749827f832b46278476e9266b39ed84"));
        Credentials credentials = Credentials.create(PRIVATE_KEY);
        this.contract = ProductVerifier.load(CONTRACT_ADDRESS, web3j, credentials, new DefaultGasProvider());
    }

    public Product addProduct(Product product) {
        Optional<Product> existingProduct = repo.findBySerialNumber(product.getSerialNumber());
        System.out.println("Using contract address: " + contract.getContractAddress());

        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product with this serial number already exists.");
        }

        try {
            // Hash serial number
            String dataToHash = product.getSerialNumber();
            byte[] hash = Hash.sha3(dataToHash.getBytes(StandardCharsets.UTF_8));

            // Register on blockchain
            TransactionReceipt receipt = contract.registerProduct(Arrays.copyOfRange(hash, 0, 32)).send();

            if (!receipt.isStatusOK()) {
                throw new RuntimeException("Blockchain transaction failed: " + receipt.getStatus());
            }
            System.out.println("Registered hash: " + bytesToHex(hash));
            System.out.println("Product registered on chain in tx: " + receipt.getTransactionHash());

            boolean isNowRegistered = false;
            try {
                isNowRegistered = contract.isProductRegistered(Arrays.copyOfRange(hash, 0, 32)).send();
            } catch (Exception ex) {
                System.err.println("Error checking product registration: " + ex.getMessage());
                // Optionally rethrow or handle gracefully
            }
            System.out.println("Verified immediately after registering: " + isNowRegistered);

            if (!isNowRegistered) {
                throw new RuntimeException("Product not registered immediately after tx!");
            }

        } catch (Exception e) {
            throw new RuntimeException("Blockchain registration failed", e);
        }

        product.setRegisteredAt(LocalDateTime.now());
        product.setAuthentic(true);
        return repo.save(product);
    }


    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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


    public Optional<Product> updateProduct(String serialNumber, Product updatedProduct) {
        Optional<Product> existing = repo.findBySerialNumber(serialNumber);
        if (existing.isPresent()) {
            Product product = existing.get();

            if (updatedProduct.getName() != null) {
                product.setName(updatedProduct.getName());
            }

            if (updatedProduct.getManufacturerCompany() != null) {
                product.setManufacturerCompany(updatedProduct.getManufacturerCompany());
            }

            if (updatedProduct.getManufacturerEmail() != null) {
                product.setManufacturerEmail(updatedProduct.getManufacturerEmail());
            }


            return Optional.of(repo.save(product));
        }
        return Optional.empty();
    }


//    verifies incoming qrcode from frontend
public Optional<Product> verifyProductFromQRCode(String serial, String timestamp, String signature) {
        Optional<Product> product = checkProduct(serial);

        if (product.isEmpty()) return Optional.empty();

        boolean isValid = qrCodeHelper.isSignatureValid(serial, timestamp, signature);
        return isValid ? product : Optional.empty();
    }


    public List<Product> getAllProducts() {
        return repo.findAll();
    }
    public boolean verifyProductOnChain(String serialNumber) {
        System.out.println("Using contract address: " + contract.getContractAddress());

        try {
            byte[] hash = Hash.sha3(serialNumber.getBytes(StandardCharsets.UTF_8));
            byte[] hash32 = Arrays.copyOfRange(hash, 0, 32);
            System.out.println("Verified hash: " + bytesToHex(hash));
            System.out.println("Verifying on-chain for serial: " + serialNumber);
            System.out.println("Hash sent to contract: " + Arrays.toString(hash32));
            try {
                Boolean isValid = contract.isProductRegistered(hash32).send();
                return isValid != null && isValid;
            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
