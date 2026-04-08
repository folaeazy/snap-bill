package com.expenseapp.app;


import com.infrastructure.persistence.config.InfraComponentScanConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.domain")
@EnableJpaRepositories(basePackages = "com.infrastructure")
@Import(InfraComponentScanConfig.class)
@EnableScheduling
public class SnapBillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapBillApplication.class, args);
    }

}
