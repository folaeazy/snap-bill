package com.infrastructure.Ai.groq;

import com.domain.gateways.AiGateway;
import com.domain.model.ExtractionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroqAiGateway implements AiGateway {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    /**
     * @param prompt prompt for llm for extraction result
     * @return Extraction result from model
     */
    @Override
    public ExtractionResult extractExpenses(String prompt) {
        GroqRequest request = new GroqRequest(
                "llama-3.3-70b-versatile",
                List.of(new GroqMessage("user", prompt)),
                0.0,
                Map.of("type", "json_object")

        );

        try {
            GroqResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(error -> new RuntimeException("Groq API error " + error))
                    ).bodyToMono(GroqResponse.class)
                    .block();

            if(response == null || response.choices().isEmpty()) {
                throw new RuntimeException("Groq response is empty");
            }
            String content = response.choices().get(0).message().content();
            return parse(content);
        } catch (Exception e) {
            log.error("LLM extraction failed ", e);
            return new ExtractionResult(null, null, null, null, null, false);

        }
    }


    private ExtractionResult parse(String content) {

        try {
            return objectMapper.readValue(content, ExtractionResult.class);
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON: {}",  e.getMessage());

            return new ExtractionResult(null, null, null, null, null, false);
        }
    }
}
