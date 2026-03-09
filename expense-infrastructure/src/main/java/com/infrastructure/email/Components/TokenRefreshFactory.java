package com.infrastructure.email.Components;

import com.domain.enums.EmailProvider;
import com.domain.interfaces.TokenRefresher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TokenRefreshFactory {

    private final Map<EmailProvider, TokenRefresher> refreshers;

    public TokenRefreshFactory(List<TokenRefresher> refresherList) {
        this.refreshers = refresherList.stream()
                .collect(Collectors.toMap(
                        TokenRefresher::provider,
                        r-> r
                ));
    }

    public TokenRefresher getRefresher(EmailProvider provider) {
        return refreshers.get(provider);
    }
}
