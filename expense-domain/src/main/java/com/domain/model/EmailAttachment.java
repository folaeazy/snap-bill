package com.domain.model;

public record EmailAttachment(
        String filename,
        String mimeType,
        byte[] content,               // raw bytes or base64
        long sizeBytes

) { }
