package com.infrastructure.email.Components;

import com.domain.enums.EmailProvider;
import com.domain.interfaces.TokenRefresher;
import com.domain.model.TokenRefreshResult;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class GoogleTokenRefresher implements TokenRefresher {


    private final String clientId = "";

    private final String clientSecret = "";

    /**
     * @param refreshToken
     * @return TokenRefreshResult
     */
    @Override
    public TokenRefreshResult refresh(String refreshToken) {

        try{
            GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    refreshToken,
                    clientId,
                    clientSecret
            ).execute();

            return TokenRefreshResult.builder()
                    .accessToken(response.getAccessToken())
                    .refreshToken(
                            response.getRefreshToken() != null
                                    ? response.getRefreshToken()
                                    : refreshToken
                    )
                    .expiresAt(
                            Instant.now().plusSeconds(response.getExpiresInSeconds())
                    )
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh google token " + e);
        }
    }

    /**
     * @return EmailProvider
     */
    @Override
    public EmailProvider provider() {
        return EmailProvider.GOOGLE;
    }
}
