package com.infrastructure.email.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@ToString
public class EmailMessageDto {

    private String id;                  // Gmail message ID
    private String subject;
    private String from;
    private Instant receivedDate;
    private String bodyText;            // plain text version
    private List<String> attachmentNames;
    private String rawContent;          // full raw for AI
}
