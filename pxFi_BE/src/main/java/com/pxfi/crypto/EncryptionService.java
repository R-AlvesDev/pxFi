package com.pxfi.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    private final TextEncryptor textEncryptor;

    public EncryptionService(@Value("${application.security.encryption.key}") String key, @Value("${application.security.encryption.salt}") String salt) {
        logger.info("Initializing EncryptionService with robust TextEncryptor.");
        // Uses AES-256 in CBC mode with PKCS5Padding, a standard and secure choice.
        this.textEncryptor = Encryptors.text(key, salt);
    }

    public String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        return textEncryptor.encrypt(data);
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        try {
            return textEncryptor.decrypt(encryptedData);
        } catch (Exception e) {
            // **THE KEY CHANGE IS HERE**: Log the full stack trace of the error.
            logger.error(
                "!!! DECRYPTION FAILED for value starting with [{}...]. THIS IS LIKELY THE ROOT CAUSE. Returning original value.",
                encryptedData.substring(0, Math.min(10, encryptedData.length())),
                e // This will print the full stack trace.
            );
            return encryptedData;
        }
    }
}

