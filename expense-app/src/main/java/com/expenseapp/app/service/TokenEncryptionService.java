package com.expenseapp.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class TokenEncryptionService {
    private final SecretKey secretKey;

    public TokenEncryptionService(@Value("${app.token.secret}") String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = new SecretKeySpec( keyBytes ,"AES");
    }

    public String encrypt(String rawToken) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder()
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
                    Base64.getDecoder().decode(encryptedToken)
            ));
        } catch (Exception e) {
            throw new IllegalStateException("Token decryption failed", e);
        }

    }
}
