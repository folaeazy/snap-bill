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

import com.infrastructure.interfaces.RetrySupplier;
import com.infrastructure.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Component("googleEmailGateway")
@Slf4j

public class GmailEmailGatewayImpl  implements EmailGateway {

    private final FinancialEmailDetector financialEmailDetector;
    private final EmailBodyExtractor emailBodyExtractor;
    private final TokenService tokenService;
    private final ExecutorService pipelineExecutor;
    private static final String APPLICATION_NAME = "SnapBill Gmail Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String USER = "me";// "me" = authenticated user

    private static final int MAX_CONCURRENT_CALL = 12;
    private static final int BATCH_SIZE = 20;
    private static final int RETRY_ATTEMPTS = 3;

    public GmailEmailGatewayImpl(FinancialEmailDetector financialEmailDetector,
                                 EmailBodyExtractor emailBodyExtractor,
                                 TokenService tokenService,
                                 @Qualifier("pipelineExecutor")ExecutorService pipelineExecutor) {
        this.financialEmailDetector = financialEmailDetector;
        this.emailBodyExtractor = emailBodyExtractor;
        this.tokenService = tokenService;
        this.pipelineExecutor = pipelineExecutor;
    }


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
            List<EmailMessageDto> metaData = fetchMetadataInBatches(gmail, account, messageIds);


            // Filter financial candidates
            List< EmailMessageDto> candidates = financialEmailDetector.filterFinancialCandidate(metaData);

            return fetchFullMessagesInBatches(gmail,candidates,account);

        }catch (Exception e) {
            log.error("Gmail fetch failed for {}: {}", account.getProviderEmail(), e.getMessage(), e);
            return List.of();
        }

    }



    //===========Helper Functions==================//

    /**
     *  GMAIL  METADATA
     * Fetches gmail meta-data in batches
     */

    private List<EmailMessageDto> fetchMetadataInBatches(Gmail gmail, EmailAccount account, List<String> messageIds) throws GeneralSecurityException, IOException {
        if (messageIds == null || messageIds.isEmpty()) return List.of();

        List<EmailMessageDto> results = Collections.synchronizedList(new ArrayList<>());
        Semaphore semaphore = new Semaphore(MAX_CONCURRENT_CALL);
        AtomicInteger i = new AtomicInteger(0);
        for (List<String> batch : partition(messageIds, BATCH_SIZE)) {
            List<CompletableFuture<Void>> futures = batch.stream()
                    .map(id -> CompletableFuture.runAsync(() -> {
                        try {
                            semaphore.acquire();
                            Message msg = retry(()->
                                    gmail.users().messages()
                                            .get(USER, id)
                                            .setFormat("metadata")
                                            .execute()
                            );
                            if(msg != null) {
                                results.add(buildMetaDataDTO(msg,account));
                            }


                        } catch (Exception e) {
                            log.error("Metadata fetch failed for id {}", id, e);
                        } finally {
                            semaphore.release();
                        }

                    }, pipelineExecutor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        }
        return results;

    }


    // ================== RETRY ==================


    private <T> T retry(RetrySupplier<T> supplier) {
        for (int i = 0; i < RETRY_ATTEMPTS; i++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                if (i == RETRY_ATTEMPTS - 1) {
                    log.error("Retry failed after {} attempts", RETRY_ATTEMPTS, e);
                    return null;
                }
            }
        }
        return null;
    }


    // ================== PARTITION ==================

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            parts.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return parts;
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


    private EmailMessageDto buildMetaDataDTO(Message messageMetadata, EmailAccount account) {
        return EmailMessageDto.builder()
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


    private List<RawEmailMessage> fetchFullMessagesInBatches(Gmail gmail, List<EmailMessageDto> candidates, EmailAccount account) {
        if(candidates.isEmpty()) return List.of();
        List<RawEmailMessage> results = Collections.synchronizedList(new ArrayList<>());
        Semaphore semaphore = new Semaphore(MAX_CONCURRENT_CALL);

         for(List<EmailMessageDto> batch : partition(candidates, BATCH_SIZE)) {
             List<CompletableFuture<RawEmailMessage>> futures = batch.stream()
                     .map(candidate -> CompletableFuture.supplyAsync(() -> {
                         try {
                             semaphore.acquire();
                             Message fullMessage = retry(()->
                                     gmail.users()
                                             .messages()
                                             .get(USER, candidate.getId())
                                             .setFormat("full")
                                             .execute()

                             );

                            return convertToRawEmail(account,fullMessage);

                         }catch (Exception e) {
                             log.error("Full message fetch failed for {}", candidate.getId(), e);
                             return null;
                         }finally {
                             semaphore.release();
                         }

                     }, pipelineExecutor))
                     .toList();

             List<RawEmailMessage> batchResults = futures.stream()
                     .map(CompletableFuture::join)
                     .toList();

             results.addAll(batchResults);

         }

            return results;

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
