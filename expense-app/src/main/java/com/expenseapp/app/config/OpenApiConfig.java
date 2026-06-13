package com.expenseapp.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI snapBillOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SnapBIll API")
                        .description("Expense tracking and analytics API")
                        .version("V1.0")
                        .contact(new Contact()
                                .name("Fola Israel")
                                .email("Folaeazy0423@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))

                );
    }
}
