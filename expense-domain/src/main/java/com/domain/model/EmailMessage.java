package com.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class EmailMessage {
    private String id;                  // Gmail ID or Graph message ID
    private String subject;
    private String from;
    private Instant receivedDate;
    private String bodyText;            // plain text or HTML stripped
    private List<String> attachments;
    private String rawContent;          // full raw email for AI parsing
}
