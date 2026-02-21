package com.expenseapp.app.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility for JWT token generation, validation, and claim extraction.
 * Uses HS512 signature algorithm.
 */

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:1800000}") // 30 minutes default
    private long expirationMs;
    Map<String, Object> claims = new HashMap<>();

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * Generate JWT token for a user.
     * Payload: subject = email, issuedAt, expiration.
     */

    public String generateToken(String email) {
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        final Date now = new Date();
        final Date expiration = new Date(now.getTime() + expirationMs);
        System.out.println("Token Generated Successfully.......");
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .and()
                .signWith(getSigningKey())
                .compact();

    }

    /**
     * Extract  identifier email/username (subject) from token.
     */

    public String extractIdentifier(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    /**
     * Validate token: signature correct + not expired + email matches.
     */

    public boolean validateToken(String token, UserDetails userDetails) {
        final String identifier = extractIdentifier(token);
        return (identifier.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction
                .apply(Jwts
                        .parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                );

    }

    private boolean isTokenExpired(String token) {
        return  extractClaims(token, Claims::getExpiration).before(new Date());
    }
}

