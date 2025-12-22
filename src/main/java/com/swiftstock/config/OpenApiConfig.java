package com.swiftstock.config;

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
                        .title("SwiftStock API 文档")
                        .description("SwiftStock 仓库管理系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SwiftStock 团队")
                                .email("support@swiftstock.com")));
    }
} 