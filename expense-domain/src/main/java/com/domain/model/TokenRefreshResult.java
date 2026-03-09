package com.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class TokenRefreshResult {
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
}
