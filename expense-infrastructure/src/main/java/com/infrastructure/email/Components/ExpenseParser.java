package com.infrastructure.email.Components;

import com.domain.model.ExtractionResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;


@Component
public class ExpenseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExtractionResult parse(String json) throws Exception{
      return objectMapper.readValue(json, ExtractionResult.class);
    }
}
