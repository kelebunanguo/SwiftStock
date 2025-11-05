package com.swiftstock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.swiftstock.mapper")
public class SwiftStockApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SwiftStockApplication.class, args);
    }
} 