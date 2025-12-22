package com.swiftstock.service;

import com.swiftstock.entity.Supplier;
import java.util.List;

/**
 * 供应商服务接口
 * - 提供对供应商的增删改查等操作接口，用于控制层注入和业务层实现解耦。
 */
 
public interface SupplierService {
    /**
     * 根据 ID 查询供应商
     *
     * @param id 供应商 ID
     * @return 供应商实体或 null
     */
    Supplier findById(Long id);

    /**
     * 查询所有供应商
     *
     * @return 供应商列表
     */
    List<Supplier> findAll();

    /**
     * 分页查询供应商
     *
     * @param name          名称模糊匹配（可空）
     * @param contactPerson 联系人（可空）
     * @param offset        偏移量
     * @param size          页大小
     * @return 供应商列表（分页）
     */
    List<Supplier> findPage(String name, String contactPerson, Integer offset, Integer size);

    /**
     * 统计符合条件的供应商数量
     *
     * @param name          名称（可空）
     * @param contactPerson 联系人（可空）
     * @return 总数
     */
    long count(String name, String contactPerson);

    /**
     * 创建供应商
     *
     * @param supplier 要创建的供应商实体
     * @return 是否创建成功
     */
    boolean create(Supplier supplier);

    /**
     * 更新供应商信息
     *
     * @param supplier 包含 ID 的供应商实体
     * @return 是否更新成功
     */
    boolean update(Supplier supplier);

    /**
     * 根据 ID 删除供应商
     *
     * @param id 供应商 ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
}


