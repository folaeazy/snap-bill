package com.infrastructure.email.gmail;

import com.domain.entities.EmailAccount;
import com.domain.gateways.EmailGateway;
import com.domain.model.EmailMessage;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.auth.oauth2.GoogleCredentials;
import com.infrastructure.email.DTO.EmailMessageDto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GmailEmailGatewayImpl  implements EmailGateway {

    private static final String APPLICATION_NAME = "SnapBill Gmail Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String USER = "me"; // "me" = authenticated user


    @Override
    public List<EmailMessage> fetchNewMessages(EmailAccount account, Instant since) {

        return List.of();
    }

    @Override
    public boolean isConnectionValid(EmailAccount account) {
        return false;
    }

    private Gmail createGmailService(EmailAccount account) throws GeneralSecurityException, IOException {
        // TODO: refresh token if expired (call token service)
        String accessToken = account.getAccessToken();

        Credential credential = new GoogleCredential().setAccessToken(accessToken);
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(httpTransport,JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

    }

    private EmailMessageDto buildEmailMessage(Message message) {
        String subject = getHeader(message, "Subject");
        String from = getHeader(message, "From");
        Instant date = Instant.ofEpochMilli(message.getInternalDate());
        String body = extractBody(message);
        List<String> attachments = extractAttachmentNames(message);

        return EmailMessageDto.builder()
                .id(message.getId())
                .subject(subject)
                .from(from)
                .receivedDate(date)
                .bodyText(body)
                .attachmentNames(attachments)
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
