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
     * @param message Raw email content (subject, body text, attachment text, etc.)
     * @return ExtractionResult containing parsed expenses + confidence + metadata
     * @throws AiGatewayException if the AI call fails (rate limit, timeout, invalid response, etc.)
     */
    ExtractionResult extractExpenses(EmailMessage message);


    /**
     * Batch extraction — useful when processing many emails in one sync.
     *
     * May be more efficient (fewer separate calls, better context).
     *
     * @param messages List of raw email messages
     * @return List of extraction results (one per message)
     */
    List<ExtractionResult> extractExpensesBatch(List<EmailMessage> messages);


    /**
     * Optional: Check health/status of the AI provider.
     * Useful for monitoring, circuit breaking, or fallback to rule-based parsing.
     */
//    default AiStatus getProviderStatus() {
//        return AiStatus.HEALTHY; // default optimistic implementation
//    }
}
