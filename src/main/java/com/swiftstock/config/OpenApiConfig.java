package com.swiftstock.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI swiftStockOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SwiftStock API Documentation")
                        .description("API documentation for SwiftStock Warehouse Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SwiftStock Team")
                                .email("support@swiftstock.com")));
    }
} 