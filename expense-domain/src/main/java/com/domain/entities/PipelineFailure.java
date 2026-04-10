package com.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class PipelineFailure {

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    private UUID accountId;
    @Column
    private String pipelineStage;
    @Column
    private String message;
    @Column
    private Instant time;
}
