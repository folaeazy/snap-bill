package com.infrastructure.security;

import com.domain.entities.EmailAccount;
import com.domain.interfaces.TokenRefresher;
import com.domain.model.TokenRefreshResult;
import com.domain.repositories.EmailAccountRepository;
import com.infrastructure.email.Components.TokenRefreshFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRefreshFactory tokenRefreshFactory;
    private final EmailAccountRepository emailAccountRepository;
    private final EncryptionService encryptionService;

    public  String getValidAccessToken(EmailAccount account) {
        String accessToken = encryptionService.decrypt(account.getAccessToken());

        if (account.getExpiresAt() != null &&
                Instant.now().isBefore(account.getExpiresAt().minusSeconds(60))) {

            return accessToken;
        }

        return refreshToken(account);
    }


    public String refreshToken(EmailAccount account) {
        String refreshToken = encryptionService.decrypt(account.getRefreshToken());
        TokenRefresher tokenRefresher = tokenRefreshFactory.getRefresher(account.getProvider());
        TokenRefreshResult result = tokenRefresher.refresh(refreshToken);

        account.setAccessToken(encryptionService.encrypt(result.getAccessToken()));
        if (result.getRefreshToken() != null) {
            account.setRefreshToken(encryptionService.encrypt(result.getRefreshToken()));
        }

        account.setExpiresAt(result.getExpiresAt());
        emailAccountRepository.save(account);

        return result.getAccessToken();

    }
}
