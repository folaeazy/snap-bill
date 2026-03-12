package com.domain.gateways;


import com.domain.exceptions.AiGatewayException;
import com.domain.model.EmailMessage;
import com.domain.model.ExtractionResult;
import java.util.*;

/**
 * Gateway interface (port) for AI-based transaction/expense extraction.
 *
 * This defines the contract for parsing raw email content into structured expense data.
 * The domain doesn't care which model/provider is used (OpenAI, Grok, Gemini, local LLM, etc.).
 *
 * Implementations live in expense-infrastructure (e.g. OpenAiGateway).
 */
public interface AiGateway  {


    /**
     * Extract expenses from a single email message.
     *
     * @param emailText Raw email content (subject, body text, attachment text, etc.)
     * @return ExtractionResult containing parsed expenses + confidence + metadata
     * @throws AiGatewayException if the AI call fails (rate limit, timeout, invalid response, etc.)
     */
    ExtractionResult extractExpenses(String emailText);


}
