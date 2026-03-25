package com.infrastructure.Ai;

import org.springframework.stereotype.Component;

@Component
public class ExpensePromptBuilder {

    public String build(String cleanedText) {
        return """
        You are a financial transaction extraction system.
    
        Extract structured transaction data from the email text below.
    
        Return ONLY valid JSON. Do not include explanations.
    
        Schema:
        {
          "transaction": boolean,
          "merchant": string,
          "amount": number,
          "currency": string,
          "category": string,
          "transactionDate": string (ISO-8601)
        }
    
        Rules:
        - If no financial transaction exists → transaction = false
        - Do NOT guess missing values
        - Amount must be a positive number (no currency symbols)
        - Currency must be a 3-letter code (e.g., NGN, USD, EUR)
        - Merchant should be the business name (e.g., Netflix, Amazon)
        - Category should be a simple word (e.g., food, transport, shopping, subscription)
        - transactionDate must be ISO-8601 format (e.g., 2026-03-25T10:15:30Z)
    
        Email Text:
        ---
        %s
        ---
        """.formatted(cleanedText);

    }
}
