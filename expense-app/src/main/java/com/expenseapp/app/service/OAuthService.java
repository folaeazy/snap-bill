package com.expenseapp.app.service;

import com.domain.entities.EmailAccount;
import com.domain.entities.User;
import com.domain.enums.AuthProvider;
import com.domain.enums.ConnectionStatus;
import com.domain.enums.EmailProvider;
import com.domain.repositories.EmailAccountRepository;
import com.domain.repositories.UserRepository;
import com.expenseapp.app.dto.OAuthResult;
import com.expenseapp.app.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {


    private final UserRepository userRepository;
    private final EmailAccountRepository emailAccountRepository;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;


    @Transactional
    public OAuthResult handle(Authentication authentication) {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {

            return OAuthResult.linked();
        }
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser != null ? oauthUser.getAttribute("email") : null;
        String name = oauthUser != null ? oauthUser.getAttribute("name") : null;
        String providerUserId = oauthUser != null ? oauthUser.getAttribute("sub") : null; // Google/Microsoft ID
        String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();

        // Extract tokens from oauth2 authorized client
        OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );

        if(client == null) {
            throw  new IllegalStateException("Auth client not found");
        }

        //Detect flow
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        //  meaning... if user is logged in  return true
        boolean linkingFlow =
                currentAuth != null && currentAuth.isAuthenticated() &&
                        !(currentAuth instanceof OAuth2AuthenticationToken);

        if(linkingFlow) {
            return linkAccount(currentAuth,provider,email,client);
        }else {
            return loginOrRegister(provider,providerUserId, email,name, client);
        }





    }

    private OAuthResult loginOrRegister(String provider, String providerUserId, String email, String name, OAuth2AuthorizedClient client) {

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setAuthProvider(AuthProvider.valueOf(provider));
                    newUser.setProviderUserId(providerUserId);
                    newUser.setEnabled(true);
                    return userRepository.save(newUser);
                });

        upsertEmailAccount(user, provider, email, client);
        return OAuthResult.issueJwt(email);
    }


    private OAuthResult linkAccount(Authentication currentAuth, String provider, String email, OAuth2AuthorizedClient client) {
        String loggedInEmail = currentAuth.getName();
        User user = userRepository.findByEmail(loggedInEmail)
                .orElseThrow();

        upsertEmailAccount(user, provider, email, client);
        return OAuthResult.linked();
    }

    private void upsertEmailAccount(User user, String provider, String email, OAuth2AuthorizedClient client) {
        Optional<EmailAccount> existing =
                emailAccountRepository
                        .findByUserIdAndProviderAndProviderEmail(
                                user.getId(),
                                EmailProvider.valueOf(provider),
                                email
                        );

        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = client.getRefreshToken() != null
                ? client.getRefreshToken().getTokenValue()
                : null;

        Instant expiresAt = client.getAccessToken().getExpiresAt();

        // Todo:  Add token encryption before saving into db
        if (existing.isPresent()) {
            EmailAccount account = existing.get();
            account.setAccessToken(accessToken);
            account.setRefreshToken(refreshToken);
            account.setExpiresAt(expiresAt);
        } else {
            EmailAccount account = new EmailAccount();
            account.setUser(user);
            account.setProvider(EmailProvider.valueOf(provider));
            account.setProviderEmail(email);
            account.setAccessToken(accessToken);
            account.setRefreshToken(refreshToken);
            account.setExpiresAt(expiresAt);
            account.setConnectedAt(Instant.now());
            account.setStatus(ConnectionStatus.ACTIVE);

            emailAccountRepository.save(account);
        }
    }




}
