package com.swiftstock.service;

import com.swiftstock.entity.SupplyRecord;

import java.util.List;

/**
 * 供货记录业务接口
 * <p>
 * 定义对 SupplyRecord 的常规业务操作：分页查询、计数、增删改查。
 * 控制器层应注入该接口进行业务调用，具体实现位于 impl 包中。
 * </p>
 */
public interface SupplyRecordService {

	/**
	 * 根据 ID 查询单条记录
	 */
	SupplyRecord findById(Long id);

	/**
	 * 分页查询指定供应商的供货记录
	 *
	 * @param supplierId 供应商 ID
	 * @param offset     偏移量
	 * @param size       页面大小
	 * @return 记录列表
	 */
	List<SupplyRecord> findPage(Long supplierId, Integer offset, Integer size);

	/**
	 * 统计指定供应商的供货记录总数
	 */
	long countBySupplierId(Long supplierId);

	/**
	 * 创建供货记录（创建时会自动计算 totalAmount，并在必要时设置 receiveTime）
	 */
	boolean create(SupplyRecord record);

	/**
	 * 更新供货记录（会重新计算 totalAmount）
	 */
	boolean update(SupplyRecord record);

	/**
	 * 根据 ID 删除记录
	 */
	boolean deleteById(Long id);
}


