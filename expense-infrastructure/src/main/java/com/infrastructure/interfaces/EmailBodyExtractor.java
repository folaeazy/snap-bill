package com.infrastructure.interfaces;

import com.domain.model.ProviderMessage;
import com.google.api.services.gmail.model.Message;

/**
 * Implemented with google only for now.....
 * TODO: // TO make for generic email Message provide type model.
 */

public interface EmailBodyExtractor {

   String extractPlainText(Message message);
   String extractHtmlBody(Message message);
}
