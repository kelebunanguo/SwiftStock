package com.swiftstock.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 配置
 *
 * <p>提供应用的 OpenAPI 元信息（中文）并注册全局 JWT Bearer 安全方案，Knife4j / Swagger UI 会读取这些信息。</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI swiftStockOpenAPI() {
        // 全局 JWT Bearer 安全方案（用于 Swagger UI 的 Authorize 按钮）
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("SwiftStock API 文档")
                        .description("SwiftStock 仓库管理系统接口文档（中文）")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SwiftStock 团队")
                                .email("support@swiftstock.com")));
    }
}