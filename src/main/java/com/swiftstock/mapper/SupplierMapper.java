package com.swiftstock.mapper;

import com.swiftstock.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 供应商Mapper
 */
@Mapper
public interface SupplierMapper {
    /**
     * 根据 ID 查询供应商
     *
     * @param id 供应商 ID
     * @return 供应商实体或 null
     */
    Supplier findById(@Param("id") Long id);

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
    List<Supplier> findPage(@Param("name") String name, @Param("contactPerson") String contactPerson, @Param("offset") Integer offset, @Param("size") Integer size);

    /**
     * 统计符合条件的供应商数量
     *
     * @param name          名称（可空）
     * @param contactPerson 联系人（可空）
     * @return 总数
     */
    long count(@Param("name") String name, @Param("contactPerson") String contactPerson);

    /**
     * 插入供应商
     *
     * @param supplier 供应商实体
     * @return 受影响的行数
     */
    int insert(Supplier supplier);

    /**
     * 更新供应商
     *
     * @param supplier 供应商实体（根据 id 更新）
     * @return 受影响的行数
     */
    int update(Supplier supplier);

    /**
     * 根据 ID 删除供应商
     *
     * @param id 供应商 ID
     * @return 受影响的行数
     */
    int deleteById(@Param("id") Long id);
}


