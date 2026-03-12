package com.infrastructure.email.service;

import com.domain.entities.RawEmailMessage;
import org.springframework.stereotype.Service;

@Service
public class TextCleaner {
    public String clean(RawEmailMessage email) {

        String text = "";

        if (email.getBody() != null && !email.getBody().isBlank()) {
            text = email.getBody();
        } else if (email.getBodyHtml() != null) {
            text = stripHtml(email.getBodyHtml());
        }

        text = removeUrls(text);
        text = removeEmailFooters(text);
        text = normalizeWhitespace(text);

        return text;
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]*>", " ");
    }

    private String removeUrls(String text) {
        return text.replaceAll("https?://\\S+", "");
    }

    private String removeEmailFooters(String text) {
        return text.replaceAll("(?i)unsubscribe.*", "");
    }

    private String normalizeWhitespace(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }
}
