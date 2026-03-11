package com.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ProviderMessage {

    private String providerMessageId;

    private String subject;

    private String sender;

    private Instant receivedDate;

    private String snippet;

    private Object Payload; //
}
