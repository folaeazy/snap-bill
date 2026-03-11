package com.infrastructure.email.service;

import com.domain.model.ProviderMessage;
import com.google.api.services.gmail.model.Message;
import com.infrastructure.interfaces.EmailBodyExtractor;
import org.springframework.stereotype.Service;

@Service("outlookBodyExtractor")
public class OutlookBodyExtractor implements EmailBodyExtractor {


    /**
     * @param message
     * @return
     */
    @Override
    public String extractPlainText(Message message) {
        return "";
    }

    /**
     * @param message
     * @return
     */
    @Override
    public String extractHtmlBody(Message message) {
        return "";
    }
}
