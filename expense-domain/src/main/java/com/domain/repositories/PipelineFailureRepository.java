package com.domain.repositories;

import com.domain.entities.PipelineFailure;

public interface PipelineFailureRepository {

    PipelineFailure save(PipelineFailure failure);
}
