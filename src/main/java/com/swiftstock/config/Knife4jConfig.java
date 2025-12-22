package com.swiftstock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Knife4jConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Knife4j 主页（确保以 /doc.html 访问）
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        // Knife4j / Swagger 静态资源（css/js 等）通常放在 webjars
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        // 有些部署会通过 /api-docs/doc.html 访问（兼容），也映射到相同的位置
        registry.addResourceHandler("/api-docs/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/api-docs/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}