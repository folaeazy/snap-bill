package com.expenseapp.app.security;

import com.domain.entities.EmailAccount;
import com.domain.entities.User;
import com.domain.enums.AuthProvider;
import com.domain.enums.ConnectionStatus;
import com.domain.enums.EmailProvider;
import com.domain.repositories.EmailAccountRepository;
import com.domain.repositories.UserRepository;
import com.expenseapp.app.util.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component("loginSuccessHandler")
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final EmailAccountRepository emailAccountRepository;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;


    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect("http://localhost:3000/me?error=auth_failed");
            return;
        }
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser != null ? oauthUser.getAttribute("email") : null;
        String name = oauthUser != null ? oauthUser.getAttribute("name") : null;
        String providerUserId = oauthUser != null ? oauthUser.getAttribute("sub") : null; // Google/Microsoft ID
        String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();

        if (email == null) {
            response.sendRedirect("/error?msg=no_email");
            return;
        }






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
        Optional<EmailAccount> existingAccount = emailAccountRepository.findByUserIdAndProviderAndProviderEmail(
                user.getId(), EmailProvider.valueOf(provider), email
        );

        if(existingAccount.isPresent()) {

            // Extract tokens from oauth2 authorized client
            OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );
            if (client != null) {
                String accessToken = client.getAccessToken().getTokenValue();
                String refreshToken = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null;
                Instant expiresAt = client.getAccessToken().getExpiresAt() != null ?
                        client.getAccessToken().getExpiresAt() : Instant.now().plusSeconds(3600);

                EmailAccount account = existingAccount.get();

                // update tokens
                // Optional: update tokens if they changed (rare, but safe)
                if (accessToken != null && !accessToken.equals(account.getAccessToken())) {
                    account.setAccessToken(accessToken);
                    account.setRefreshToken(refreshToken);
                    account.setExpiresAt(expiresAt);
                    emailAccountRepository.save(account);
                    log.info("Updated tokens for existing EmailAccount {}", account.getId());
                }
            }

        }else {

            OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            if (client == null) {
                response.sendRedirect("/error?msg=no_client");
                return;
            }

            // Create first EmailAccount (the one used for login/signup)
            EmailAccount emailAccount = new EmailAccount();
            emailAccount.setId(UUID.randomUUID());
            emailAccount.setUser(user);
            emailAccount.setProvider(EmailProvider.valueOf(provider));
            emailAccount.setProviderEmail(email);
            emailAccount.setAccessToken(client.getAccessToken().getTokenValue());
            emailAccount.setRefreshToken((client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null) != null ? client.getRefreshToken().getTokenValue() : null);
            emailAccount.setExpiresAt(client.getAccessToken().getTokenValue() != null ? client.getAccessToken().getExpiresAt() : Instant.now().plusSeconds(3600));
            emailAccount.setConnectedAt(Instant.now());
            emailAccount.setLastSyncAt(null); // triggers full initial sync
            emailAccount.setStatus(ConnectionStatus.ACTIVE);

            emailAccountRepository.save(emailAccount);

            log.info("Created first EmailAccount for user {} with provider {}", user.getId(), provider);
        }



    // Generate JWT
        String jwt = jwtUtils.generateToken(user.getEmail());

            // Redirect to frontend with JWT (or set cookie)
        String redirectUrl = "http://localhost:3000/dashboard?token=" + jwt; // adjust to your frontend
        response.sendRedirect(redirectUrl);


    }


}
