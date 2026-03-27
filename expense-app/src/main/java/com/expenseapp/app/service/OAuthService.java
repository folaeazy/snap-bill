package com.expenseapp.app.service;

import com.domain.entities.EmailAccount;
import com.domain.entities.User;
import com.domain.enums.AuthProvider;
import com.domain.enums.ConnectionStatus;
import com.domain.enums.EmailProvider;
import com.domain.repositories.EmailAccountRepository;
import com.domain.repositories.UserRepository;
import com.expenseapp.app.dto.OAuthResult;
import com.infrastructure.email.service.EmailSyncService;
import com.infrastructure.email.service.ExpenseExtractionService;
import com.infrastructure.scheduling.ExpenseExtractionJob;
import com.infrastructure.security.EncryptionService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
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
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OAuthService {


    @Qualifier("applicationTaskExecutor")
    private final TaskExecutor  taskExecutor;
    private final UserRepository userRepository;
    private final EmailAccountRepository emailAccountRepository;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final EncryptionService tokenEncryptionService;
    private final EmailSyncService emailSyncService;
    private final ExpenseExtractionJob expenseExtractionJob;

    public OAuthService(
            @Qualifier("applicationTaskExecutor")
            TaskExecutor taskExecutor,
            UserRepository userRepository,
            EmailAccountRepository emailAccountRepository,
            OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
            EncryptionService tokenEncryptionService,
            EmailSyncService emailSyncService,
            ExpenseExtractionJob expenseExtractionJob
    ) {
        this.taskExecutor = taskExecutor;
        this.userRepository = userRepository;
        this.emailAccountRepository = emailAccountRepository;
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
        this.tokenEncryptionService = tokenEncryptionService;
        this.emailSyncService = emailSyncService;
        this.expenseExtractionJob = expenseExtractionJob;

    }


    @Transactional
    public OAuthResult handle(Authentication authentication) {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

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

        EmailAccount account = upsertEmailAccount(user, provider, email, client);
        // Email Sync trigger

        CompletableFuture
                .supplyAsync(()-> emailSyncService.syncAccount(account), taskExecutor)
                .thenApply(processed -> {
                    log.info("Sync done on thread name : {}", Thread.currentThread());
                    return processed;
                })
                .thenCompose(processed -> {
                    if(processed > 0) {
                        return CompletableFuture.runAsync(expenseExtractionJob::processPendingEmails, taskExecutor);
                    }
                    return CompletableFuture.completedFuture(null);
                })
                .exceptionally(ex -> {
                    log.error("Pipeline failed for account {}", account.getId(), ex);
                    return null;
                });
        return OAuthResult.issueJwt(email);
    }


    private OAuthResult linkAccount(Authentication currentAuth, String provider, String email, OAuth2AuthorizedClient client) {
        String loggedInEmail = currentAuth.getName();
        User user = userRepository.findByEmail(loggedInEmail)
                .orElseThrow();

        EmailAccount account = upsertEmailAccount(user, provider, email, client);
        CompletableFuture
                .supplyAsync(()-> emailSyncService.syncAccount(account), taskExecutor)
                .thenApply(processed -> {
                    log.info("Sync done during email linking on thread name : {}", Thread.currentThread());
                    return processed;
                })
                .thenCompose(processed -> {
                    if(processed > 0) {
                        return CompletableFuture.runAsync(expenseExtractionJob::processPendingEmails, taskExecutor);
                    }
                    return CompletableFuture.completedFuture(null);
                })
                .exceptionally(ex -> {
                    log.error("Pipeline failed for account {}", account.getId(), ex);
                    return null;
                });
        return OAuthResult.linked();
    }

    private EmailAccount upsertEmailAccount(User user, String provider, String email, OAuth2AuthorizedClient client) {
        Optional<EmailAccount> existing =
                emailAccountRepository
                        .findByUserIdAndProviderAndProviderEmail(
                                user.getId(),
                                EmailProvider.valueOf(provider),
                                email
                        );
        String existingRefreshToken = "";
        if(existing.isPresent() && existing.get().getRefreshToken() != null){
            existingRefreshToken = existing.get().getRefreshToken();
        }
        EmailAccount account;

        String accessToken = tokenEncryptionService.encrypt(client.getAccessToken().getTokenValue());
        String refreshToken = client.getRefreshToken() != null
                ? tokenEncryptionService.encrypt(client.getRefreshToken().getTokenValue())
                : existingRefreshToken ;
        // Todo:  Remove later
        System.out.printf("This is the REFRESH TOKEN FROM LOGIN %s", refreshToken);

        Instant expiresAt = client.getAccessToken().getExpiresAt();


        if (existing.isPresent()) {
            account = existing.get();
            account.setAccessToken(accessToken);
            account.setRefreshToken(refreshToken);
            account.setExpiresAt(expiresAt);
        } else {
            account = new EmailAccount();
            account.setUser(user);
            account.setProvider(EmailProvider.valueOf(provider));
            account.setProviderEmail(email);
            account.setAccessToken(accessToken);
            account.setRefreshToken(refreshToken);
            account.setExpiresAt(expiresAt);
            account.setConnectedAt(Instant.now());
            account.setStatus(ConnectionStatus.ACTIVE);

        }
       return emailAccountRepository.save(account);
    }




}
