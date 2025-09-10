package com.effective.mobile.bank_card_manager.service.impl;

import com.effective.mobile.bank_card_manager.service.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EncryptionServiceImpl implements EncryptionService {
    // Мы добавим этот ключ в application-local.yml
    @Value("${app.encryption.key}")
    private String encryptionKey;
    private static final String ALGORITHM = "AES";

    private SecretKeySpec getKey() {
        // ДЕКОДИРУЕТ КЛЮЧ ИЗ BASE64
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
        // AES требует ключ длиной 16, 24 или 32 байта
        if (decodedKey.length > 32) {
            decodedKey = java.util.Arrays.copyOf(decodedKey, 32);
        } else if (decodedKey.length < 16) {
            throw new RuntimeException("Ключ шифрования после декодирования Base64 должен быть не менее 16 байт.");
        }
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }

    @Override
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании", e);
        }
    }

    @Override
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при расшифровке", e);
        }
    }

    @Override
    public String maskCardNumber(String fullCardNumber) {
        if (fullCardNumber == null || fullCardNumber.length() < 4) {
            return "****";
        }
        // Оставляем последние 4 цифры
        String last4 = fullCardNumber.substring(fullCardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}