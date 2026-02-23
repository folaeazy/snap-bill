package com.domain.exceptions;

public class AiGatewayException extends DomainValidationException{
    public AiGatewayException(String message) {
        super(message);
    }
}
