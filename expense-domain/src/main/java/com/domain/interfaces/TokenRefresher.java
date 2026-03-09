package com.domain.interfaces;

import com.domain.enums.EmailProvider;
import com.domain.model.TokenRefreshResult;

public interface TokenRefresher {
    TokenRefreshResult refresh(String refreshToken);
    EmailProvider provider();
}
