package com.infrastructure.Ai.groq;


import java.util.List;

public record GroqResponse(
        List<Choice> choices
) {
    public record Choice( Message message) {}
    public record Message( String content) {}
}
