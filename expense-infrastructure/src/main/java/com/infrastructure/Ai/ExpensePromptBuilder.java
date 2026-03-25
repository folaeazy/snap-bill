package com.infrastructure.Ai;

import org.springframework.stereotype.Component;

@Component
public class ExpensePromptBuilder {

    public String build(String subject, String body) {

        return """
        You are a financial data extraction system.

        Extract transaction details from the email below.

        Return ONLY valid JSON.

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
        - Do NOT guess values
        - Currency must be like USD, NGN, EUR
        - Amount must be a number only

        Email:
        ---
        Subject: %s

        Body:
        %s
        ---
        """.formatted(subject, body);
    }
}
