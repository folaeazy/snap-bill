package com.infrastructure.persistence.config;


import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

/**
 * Central configuration for JPA entities and repositories.
 * Imported by expense-app to activate persistence layer.
 */
@Configuration
@ComponentScan("com.infrastructure")
public class PersistenceConfig {
}
