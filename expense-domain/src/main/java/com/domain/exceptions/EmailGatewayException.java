package com.domain.exceptions;

public class EmailGatewayException extends DomainValidationException{
    public EmailGatewayException(String message) {
        super(message);
    }
}
