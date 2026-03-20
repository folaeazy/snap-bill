package com.infrastructure.Ai.groq;

import java.util.List;
import java.util.Map;

public record GroqRequest (
        String model,
        List<GroqMessage> messages,
        double temperature,
        Map<String, String> response_format
)
{ }
