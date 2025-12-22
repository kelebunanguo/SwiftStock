package com.swiftstock.service;

import com.swiftstock.entity.Category;
import java.util.List;

/**
 * 商品分类服务接口。
 * <p>
 * 说明（中文）：
 * - 将分类相关的 CRUD 操作抽象为接口，具体实现位于 `com.swiftstock.service.impl.CategoryServiceImpl`。
 * - 使用接口有利于解耦和单元测试替换实现。
 */
public interface CategoryService {

    /**
     * 根据 ID 查询分类。
     *
     * @param id 分类 ID
     * @return 分类对象或 null
     */
    Category findById(Long id);

    /**
     * 查询所有分类。
     *
     * @return 分类列表
     */
    List<Category> findAll();

    /**
     * 保存或更新分类；若 id 为空则为创建，否则为更新。
     *
     * @param category 分类对象
     */
    void save(Category category);

    /**
     * 根据 ID 删除分类。
     *
     * @param id 分类 ID
     */
    void delete(Long id);
}