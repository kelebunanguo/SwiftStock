package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Admin {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
} 