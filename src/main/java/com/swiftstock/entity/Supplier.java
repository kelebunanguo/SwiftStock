package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商实体
 */
@Data
public class Supplier {
    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}


