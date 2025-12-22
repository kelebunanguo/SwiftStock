package com.swiftstock.mapper;

import com.swiftstock.entity.SupplyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SupplyRecordMapper {
	/**
	 * 根据 supplierId 查询所有供货记录（不分页）
	 */
	List<SupplyRecord> findBySupplierId(@Param("supplierId") Long supplierId);

	/**
	 * 分页查询指定供应商的供货记录
	 *
	 * @param supplierId 供应商 ID
	 * @param offset     偏移量
	 * @param size       页大小
	 */
	List<SupplyRecord> findPage(@Param("supplierId") Long supplierId,
								@Param("offset") Integer offset,
								@Param("size") Integer size);

	/**
	 * 统计指定供应商的记录总数
	 */
	long countBySupplierId(@Param("supplierId") Long supplierId);

	/**
	 * 根据 ID 查询单条记录
	 */
	SupplyRecord findById(@Param("id") Long id);

	/**
	 * 插入一条记录（使用 useGeneratedKeys 返回自增主键）
	 */
	int insert(SupplyRecord record);

	/**
	 * 更新记录（根据 record.id）
	 */
	int update(SupplyRecord record);

	/**
	 * 根据 ID 删除记录
	 */
	int deleteById(@Param("id") Long id);
}


