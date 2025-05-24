package com.truescan.truescan_backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class QRCodeHelper {

    @Value("${secret.key}")
    private String secretKey;

    public String generateQRCodeBase64(String serial, int width, int height) {
        try {
            String payload = generateSecurePayload(serial);
            byte[] qrBytes = generateQRCodeImage(payload, width, height);
            return Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate QR Code", e);
        }
    }

    private String generateSecurePayload(String serial) {
        Map<String, String> payload = new HashMap<>();
        String timestamp = Instant.now().toString();

        String signature = generateSignature(serial, timestamp);
        payload.put("serial", serial);
        payload.put("timestamp", timestamp);
        payload.put("signature", signature);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(payload);   //changing the map to json
        } catch (Exception e) {
            throw new RuntimeException("JSON creation failed");
        }
    }

    private String generateSignature(String serial, String timestamp) {
        try {
            String data = serial + timestamp;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed");
        }
    }

    private byte[] generateQRCodeImage(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("QR to image failed");
        }
    }

//    This one checks the signature
    public boolean isSignatureValid(String serial, String timestamp, String providedSignature) {
        try {
            String data = serial + timestamp;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = Base64.getEncoder().encodeToString(hash);
            System.out.println("Verifying signature:");
            System.out.println("Serial: " + serial);
            System.out.println("Timestamp: " + timestamp);
            System.out.println("Expected Signature: " + generatedSignature);
            System.out.println("Provided Signature: " + providedSignature);

            return generatedSignature.equals(providedSignature);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

}
