package com.expenseapp.app.config;


import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.infrastructure.repositories" // your infra repos
)
@EntityScan(basePackages = "com.domain.entities") // your domain entities
public class PersistenceConfig {
}
