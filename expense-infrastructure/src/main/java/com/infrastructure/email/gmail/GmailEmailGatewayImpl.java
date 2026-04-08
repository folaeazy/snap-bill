package com.infrastructure.email.gmail;

import com.domain.entities.EmailAccount;
import com.domain.entities.RawEmailMessage;
import com.domain.gateways.EmailGateway;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.domain.model.EmailMessageDto;
import com.infrastructure.email.Components.FinancialEmailDetector;
import com.infrastructure.interfaces.EmailBodyExtractor;

import com.infrastructure.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component("googleEmailGateway")
@Slf4j
@RequiredArgsConstructor
public class GmailEmailGatewayImpl  implements EmailGateway {

    private final FinancialEmailDetector financialEmailDetector;
    private final EmailBodyExtractor emailBodyExtractor;
    private final TokenService tokenService;
    private static final String APPLICATION_NAME = "SnapBill Gmail Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String USER = "me"; // "me" = authenticated user

    // TODO: inject from config or secrets




    @Override
    public List<RawEmailMessage> fetchNewMessages(EmailAccount account, Instant since) {

        try{

            Gmail gmail = createGmailService(account);
            Instant fetchSince = since != null
                    ? since
                    : Optional.ofNullable(account.getLastEmailReceivedAt())
                    .orElse(Instant.now().minus(30, ChronoUnit.DAYS));

            List<String> messageIds = fetchMessageIds(gmail, fetchSince);
            if(messageIds.isEmpty()) {
                log.info("No new Gmail message  for account {} is empty", account.getProviderEmail());
                return List.of();
            }

            // Fetch email meta-data concurrently
            List<EmailMessageDto> metaData = fetchMetadataConcurrently(gmail, account, messageIds);


            // Filter financial candidates
            List< EmailMessageDto> candidates = financialEmailDetector.filterFinancialCandidate(metaData);

            return fetchFullMessages(gmail,candidates,account);




        }catch (Exception e) {
            log.error("Gmail fetch failed for {}: {}", account.getProviderEmail(), e.getMessage(), e);
            return List.of();
        }

    }



    //===========Helper Functions==================//

    private List<EmailMessageDto> fetchMetadataConcurrently(Gmail gmail, EmailAccount account, List<String> messageIds) throws GeneralSecurityException, IOException {
        if(messageIds == null || messageIds.isEmpty()) return List.of();

        List<EmailMessageDto> rawEmailMessages = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger i = new AtomicInteger(0);
        try {

            //virtual-thread executor
            try(ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Void>> futures = messageIds.stream()
                        .map(id -> CompletableFuture.runAsync(()-> {
                            try{
                                Message messageMetadata = gmail.users().messages()
                                        .get(USER, id)
                                        .setFormat("metadata") //meta only
                                        .execute();

                                EmailMessageDto  raw = EmailMessageDto.builder()
                                        .id(messageMetadata.getId())
                                        .emailAccount(account)
                                        .provider(account.getProvider())
                                        .providerMessageId(messageMetadata.getId())
                                        .subject(getHeader(messageMetadata, "Subject"))
                                        .to(getHeader(messageMetadata, "To"))
                                        .sender(getHeader(messageMetadata, "From"))
                                        .receivedDate(Instant.ofEpochMilli(messageMetadata.getInternalDate()))
                                        .snippet(messageMetadata.getSnippet())
                                        .build();

                                rawEmailMessages.add(raw);
                                System.out.println("Current thread name : "  + Thread.currentThread() + " " + i.incrementAndGet());
                            } catch (IOException e) {
                                throw new RuntimeException(e);

                            }
                        }, executor))
                        .toList();
                //wait for all thread to complete
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                log.info("Fetched metadata for {} messages for account {}", rawEmailMessages.size(), account.getProviderEmail());
                return rawEmailMessages;

            }


        }catch (Exception e) {
            log.error(e.getMessage());
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


    private List<RawEmailMessage> fetchFullMessages(Gmail gmail, List<EmailMessageDto> candidates, EmailAccount account) {
        if(candidates.isEmpty()) return List.of();

        try (ExecutorService taskExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<RawEmailMessage>> futures = candidates.stream()
                    .map(candidate ->
                            CompletableFuture.supplyAsync(() -> {
                                try {
                                    Message fullMessage = gmail.users()
                                            .messages()
                                            .get(USER, candidate.getId())
                                            .setFormat("full")
                                            .execute();

                                    return convertToRawEmail(account, fullMessage);

                                } catch (Exception e) {
                                    log.error(
                                            "Failed to fetch full message {} for account {}",
                                            candidate.getId(),
                                            account.getProviderEmail(),
                                            e
                                    );

                                    return null;
                                }

                            }, taskExecutor)
                    )
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();
        }


    }

    private RawEmailMessage convertToRawEmail(EmailAccount account, Message message) {
        String subject = getHeader(message, "Subject");
        String sender = getHeader(message, "From");

        String snippet = message.getSnippet();

        String body = emailBodyExtractor.extractPlainText(message);
        String bodyHtml = emailBodyExtractor.extractHtmlBody(message);

        List<String> attachments = extractAttachmentNames(message);

        RawEmailMessage rawEmail = new RawEmailMessage();

        rawEmail.setEmailAccount(account);
        rawEmail.setProviderMessageId(message.getId());
        rawEmail.setSubject(subject);
        rawEmail.setSender(sender);
        rawEmail.setProvider(account.getProvider());
        rawEmail.setTo("me");
        rawEmail.setSnippet(snippet);
        rawEmail.setBody(body);
        rawEmail.setBodyHtml(bodyHtml);
        rawEmail.setThreadId(message.getThreadId());
        rawEmail.setAttachments(attachments);
        rawEmail.setReceivedDate(
                Instant.ofEpochMilli(message.getInternalDate())
        );

        return rawEmail;
    }


    private Gmail createGmailService(EmailAccount account) throws GeneralSecurityException, IOException {

        String accessToken = tokenService.getValidAccessToken(account);

        HttpRequestInitializer initializer = request ->
                request.getHeaders().setAuthorization("Bearer " + accessToken);

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(httpTransport,JSON_FACTORY, initializer)
                .setApplicationName(APPLICATION_NAME)
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
