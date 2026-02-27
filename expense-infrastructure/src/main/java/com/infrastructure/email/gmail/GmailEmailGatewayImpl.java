package com.infrastructure.email.gmail;

import com.domain.entities.EmailAccount;
import com.domain.gateways.EmailGateway;
import com.domain.model.EmailAttachment;
import com.domain.model.EmailMessage;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.infrastructure.email.DTO.EmailMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component("gmailEmailGateway")
@Slf4j
@RequiredArgsConstructor
public class GmailEmailGatewayImpl  implements EmailGateway {

    private static final String APPLICATION_NAME = "SnapBill Gmail Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String USER = "me"; // "me" = authenticated user

    // TODO: inject from config or secrets
    private final String clientId;
    private final String clientSecret;

    @Override
    public List<EmailMessage> fetchNewMessages(EmailAccount account, Instant since) {

        try{

            Gmail service = createGmailService(account);
            Instant fetchSince = since != null ? since : account.getLastSyncAt();
            if(fetchSince == null) {
                fetchSince = Instant.now().minus(30, ChronoUnit.DAYS);
            }

            long sinceSeconds = fetchSince.getEpochSecond();
            String query = "after:" + sinceSeconds + " category:primary";
            ListMessagesResponse response = service.users().messages()
                    .list(USER)
                    .setQ(query)
                    .setMaxResults(50L)
                    .execute();

            List<EmailMessage> messages = new ArrayList<>();

            if (response.getMessages() != null) {
                for (Message msgSummary : response.getMessages()) {
                    Message fullMessage = service.users().messages()
                            .get(USER, msgSummary.getId())
                            .setFormat("full")
                            .execute();

                    messages.add(buildEmailMessage(fullMessage));
                }
            }
            log.info("Gmail: fetched {} messages since {} for {}", messages.size(), fetchSince, account.getProviderEmail());
            return messages;

        }catch (Exception e) {
            log.error("Gmail fetch failed for {}: {}", account.getProviderEmail(), e.getMessage(), e);
            return List.of();
        }

    }

    @Override
    public boolean isConnectionValid(EmailAccount account) {
        try {
            Gmail service = createGmailService(account);
            service.users().labels().list(USER).execute(); // lightweight test call
            return true;
        } catch (Exception e) {
            log.warn("Gmail connection invalid for {}: {}", account.getProviderEmail(), e.getMessage());
            return false;
        }
    }

    private Gmail createGmailService(EmailAccount account) throws GeneralSecurityException, IOException {
        // TODO: refresh token if expired (call token service)
        String accessToken = refreshAccessTokenIfNeeded(account);

        GoogleCredentials credential = GoogleCredentials
                .newBuilder()
                .setAccessToken(new AccessToken(accessToken, null))
                .build();

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(httpTransport,JSON_FACTORY, new HttpCredentialsAdapter(credential))
                .setApplicationName(APPLICATION_NAME)
                .build();

    }

    private String refreshAccessTokenIfNeeded(EmailAccount account) throws IOException, GeneralSecurityException {
        if (account.getExpiresAt() == null || Instant.now().isBefore(account.getExpiresAt())) {
            return account.getAccessToken();
        }

        // Real refresh
        GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                account.getRefreshToken(),
                clientId,
                clientSecret
        ).execute();

        String newToken = tokenResponse.getAccessToken();
        account.setAccessToken(newToken);
        account.setExpiresAt(Instant.now().plusSeconds(tokenResponse.getExpiresInSeconds()));

        if (tokenResponse.getRefreshToken() != null) {
            account.setRefreshToken(tokenResponse.getRefreshToken());
        }

        // TODO: save updated account via EmailAccountRepository
        // emailAccountRepository.save(account);

        log.info("Refreshed Gmail access token for {}", account.getProviderEmail());
        return newToken;
    }

    private EmailMessage buildEmailMessage(Message message) {
        String subject = getHeader(message, "Subject");
        String from = getHeader(message, "From");
        Instant date = Instant.ofEpochMilli(message.getInternalDate());
        String body = extractBody(message);
        List<String> attachments = extractAttachmentNames(message);

        return EmailMessage.builder()
                .id(message.getId())
                .subject(subject)
                .from(from)
                .receivedDate(date)
                .bodyText(body)
                .attachments(attachments)
                .rawContent(message.toString())
                .build();

    }

    private List<String> extractAttachmentNames(Message message) {
        List<String> names = new ArrayList<>();
        if (message.getPayload() != null && message.getPayload().getParts() != null) {
            message.getPayload().getParts().stream()
                    .filter(part -> part.getFilename() != null && !part.getFilename().isEmpty())
                    .forEach(part -> names.add(part.getFilename()));
        }
        return names;
    }

    private String extractBody(Message message) {

        if (message.getPayload() == null) return "";

        MessagePart payload = message.getPayload();

        if (payload.getBody() != null && payload.getBody().getData() != null) {
            return new String(Base64.getUrlDecoder().decode(payload.getBody().getData()));
        }

        if (payload.getParts() != null) {
            for (MessagePart part : payload.getParts()) {
                if ("text/plain".equals(part.getMimeType()) && part.getBody().getData() != null) {
                    return new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
                }
            }
        }
        return "";
    }

    private String getHeader(Message message, String name) {
        if (message.getPayload() == null || message.getPayload().getHeaders() == null) return "";
        return message.getPayload().getHeaders().stream()
                .filter(h -> name.equalsIgnoreCase(h.getName()))
                .map(MessagePartHeader::getValue)
                .findFirst()
                .orElse("");
    }
}
