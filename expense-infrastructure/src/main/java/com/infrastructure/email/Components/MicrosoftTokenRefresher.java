package com.infrastructure.email.Components;

import com.domain.enums.EmailProvider;
import com.domain.interfaces.TokenRefresher;
import com.domain.model.TokenRefreshResult;
import org.springframework.stereotype.Component;

@Component
public class MicrosoftTokenRefresher implements TokenRefresher {
    /**
     * @param refreshToken
     * @return
     */
    @Override
    public TokenRefreshResult refresh(String refreshToken) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public EmailProvider provider() {
        return null;
    }
}
