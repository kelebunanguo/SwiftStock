package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类实体
 *
 * <p>用于表示商品的分类层级信息，支持父子关系与路径表示，供分类管理与商品关联使用。</p>
 */
@Data
public class Category {
    private Long id;
    private String name;
    private Long parentId;        // 父级分类ID
    private Integer level;         // 分类层级：1-一级分类，2-二级分类，3-三级分类
    private Integer sortOrder;    // 排序字段
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    // 关联字段
    private List<Category> children;  // 子分类列表
    private String fullPath;          // 完整分类路径，如：服装-女装-连衣裙
}