package com.swiftstock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用程序启动类
 *
 * <p>负责启动 Spring Boot 应用并扫描 MyBatis Mapper 接口。</p>
 */
@SpringBootApplication
@MapperScan("com.swiftstock.mapper")
public class SwiftStockApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SwiftStockApplication.class, args);
    }
} 