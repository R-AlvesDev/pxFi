package com.pxfi.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    private final TextEncryptor textEncryptor;

    public EncryptionService(@Value("${application.security.encryption.key}") String key, @Value("${application.security.encryption.salt}") String salt) {
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
            return encryptedData;
        }
    }
}