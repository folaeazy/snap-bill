package com.expenseapp.app;


import com.infrastructure.persistence.config.PersistenceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.domain")
@EnableJpaRepositories(basePackages = "com.infrastructure")
@Import(PersistenceConfig.class)
public class SnapBillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapBillApplication.class, args);
    }

}
