package com.infrastructure.email.gmail;

import com.domain.entities.EmailAccount;
import com.domain.entities.RawEmailMessage;
import com.domain.gateways.EmailGateway;
import com.domain.model.EmailMessage;
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
import java.util.Optional;

@Component("googleEmailGateway")
@Slf4j
@RequiredArgsConstructor
public class GmailEmailGatewayImpl  implements EmailGateway {

    private static final String APPLICATION_NAME = "SnapBill Gmail Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String USER = "me"; // "me" = authenticated user

    // TODO: inject from config or secrets


    private final String clientId = "";

    private final String clientSecret = "";

    @Override
    public List<EmailMessage> fetchNewMessages(EmailAccount account, Instant since) {

        try{

            Gmail gmail = createGmailService(account);
            Instant fetchSince = since != null
                    ? since
                    : Optional.ofNullable(account.getLastSyncAt())
                    .orElse(Instant.now().minus(30, ChronoUnit.DAYS));

            List<String> messageIds = fetchMessageIds(gmail, fetchSince);
            if(messageIds.isEmpty()) {
                log.info("No new Gmail message  for account {} is empty", account.getProviderEmail());
                return List.of();
            }

            log.info("Gmail metadata fetched {}  successfully for account {}",messageIds.size(), account.getProviderEmail());

            // Fetch email body concurrently
            List<EmailMessage> messages = fetchMessagesConcurrently(gmail, messageIds);
            log.info("Fetched {} full Gmail messages for {}",
                    messages.size(),
                    account.getProviderEmail());

            return messages;


        }catch (Exception e) {
            log.error("Gmail fetch failed for {}: {}", account.getProviderEmail(), e.getMessage(), e);
            return List.of();
        }

    }

    private List<RawEmailMessage> fetchMessagesConcurrently(Gmail gmail, List<String> messageIds) {
        if(messageIds == null || messageIds.isEmpty()) return Li
    }

    private List<String> fetchMessageIds(Gmail gmail, Instant fetchSince) throws IOException {
        List<String> ids =  new ArrayList<>();
        long sinceInSeconds = fetchSince.getEpochSecond();

        String query = "after:" + sinceInSeconds + " category:primary";
        String pageToken =  null;

        do {

            ListMessagesResponse response = gmail.users()
                    .messages()
                    .list(USER)
                    .setQ(query)
                    .setMaxResults(500L)
                    .setPageToken(pageToken)
                    .execute();

            if(response.getMessages() != null) {
                for(Message message : response.getMessages()) {
                    ids.add(message.getId());
                }
            }
            pageToken = response.getNextPageToken();
        }while (pageToken != null);

        log.info("Gmail returned {} message ids since {}", ids.size(), fetchSince);
        return  ids;

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
