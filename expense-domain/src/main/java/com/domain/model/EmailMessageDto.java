package com.domain.model;

import com.domain.entities.EmailAccount;
import com.domain.enums.EmailProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@ToString
public class EmailMessageDto {

    private String id; // Gmail message ID
    private EmailAccount emailAccount;
    private EmailProvider provider;
    private String threadId;
    private String providerMessageId;
    private String subject;
    private String from;
    private String to;
    private String snippet;
    private Instant receivedDate;
    private String bodyText;            // plain text version
    private List<String> attachmentNames;

}
