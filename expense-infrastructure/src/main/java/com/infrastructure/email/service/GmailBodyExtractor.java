package com.infrastructure.email.service;

import com.domain.model.ProviderMessage;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.infrastructure.interfaces.EmailBodyExtractor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service("gmailBodyExtractor")
@Primary
public class GmailBodyExtractor implements EmailBodyExtractor {
    /**
     * @param message
     * @return
     */
    @Override
    public String extractPlainText(Message message) {


        if (message.getPayload() == null) {
            return "";
        }

        return extractPlainTextFromPart(message.getPayload());
    }

    /**
     * @param message
     * @return
     */
    @Override
    public String extractHtmlBody(Message message) {


        if (message.getPayload() == null) {
            return "";
        }

        return extractHtmlFromPart(message.getPayload());
    }



    private String extractPlainTextFromPart(MessagePart part) {

        if ("text/plain".equalsIgnoreCase(part.getMimeType())
                && part.getBody() != null
                && part.getBody().getData() != null) {

            return decode(part.getBody().getData());
        }

        if (part.getParts() != null) {

            for (MessagePart subPart : part.getParts()) {

                String result = extractPlainTextFromPart(subPart);

                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        return "";
    }


    private String extractHtmlFromPart(MessagePart part) {

        if ("text/html".equalsIgnoreCase(part.getMimeType())
                && part.getBody() != null
                && part.getBody().getData() != null) {

            return decode(part.getBody().getData());
        }

        if (part.getParts() != null) {

            for (MessagePart subPart : part.getParts()) {

                String result = extractHtmlFromPart(subPart);

                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        return "";
    }


    private String decode(String data) {
        return new String(Base64.getUrlDecoder().decode(data));
    }

}
