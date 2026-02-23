package com.infrastructure.persistence.config;


import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Central configuration for JPA entities and repositories.
 * Imported by expense-app to activate persistence layer.
 */
@Configuration
public class PersistenceConfig {
}
