package com.swiftstock.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 配置
 *
 * <p>提供应用的 OpenAPI 元信息，用于在 Swagger UI 中展示接口文档标题、描述等信息。</p>
 */
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