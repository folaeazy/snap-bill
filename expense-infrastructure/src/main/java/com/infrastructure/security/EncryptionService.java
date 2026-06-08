package com.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {
    private final SecretKey secretKey;

    public EncryptionService(@Value("${encryption.token.secret}") String secret) {
        byte[] keyBytes = Base64.getMimeDecoder().decode(secret.trim());
        this.secretKey = new SecretKeySpec( keyBytes ,"AES");
    }

    public String encrypt(String rawToken) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getMimeEncoder()
                    .encodeToString(cipher.doFinal(rawToken.getBytes()));

        }catch(Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String decrypt(String encryptedToken) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(
                    Base64.getMimeDecoder().decode(encryptedToken)
            ));
        } catch (Exception e) {
            throw new IllegalStateException("Token decryption failed", e);
        }

    }
}
