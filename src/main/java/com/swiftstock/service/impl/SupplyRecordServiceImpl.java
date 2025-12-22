package com.swiftstock.service.impl;

import com.swiftstock.entity.SupplyRecord;
import com.swiftstock.mapper.SupplyRecordMapper;
import com.swiftstock.service.SupplyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供货记录服务实现类（SupplyRecordService 的实现）
 *
 * <p>责任：
 * <ul>
 *   <li>提供按供应商分页查询供货记录</li>
 *   <li>创建/更新时自动计算 totalAmount 并在创建时默认设置 receiveTime</li>
 *   <li>通过 SupplyRecordMapper 与数据库交互</li>
 * </ul>
 */
@Service
public class SupplyRecordServiceImpl implements SupplyRecordService {

	@Autowired
	private SupplyRecordMapper supplyRecordMapper;

	/**
	 * 根据 ID 查询单条供货记录
	 *
	 * @param id 记录 ID
	 * @return 对应的 SupplyRecord 或 null
	 */
	@Override
	public SupplyRecord findById(Long id) {
		return supplyRecordMapper.findById(id);
	}

	/**
	 * 分页查询指定供应商的供货记录
	 *
	 * @param supplierId 供应商 ID
	 * @param offset     偏移量
	 * @param size       页大小
	 * @return 供货记录列表
	 */
	@Override
	public List<SupplyRecord> findPage(Long supplierId, Integer offset, Integer size) {
		return supplyRecordMapper.findPage(supplierId, offset, size);
	}

	/**
	 * 统计指定供应商的供货记录总数
	 *
	 * @param supplierId 供应商 ID
	 * @return 总记录数
	 */
	@Override
	public long countBySupplierId(Long supplierId) {
		return supplyRecordMapper.countBySupplierId(supplierId);
	}

	/**
	 * 创建供货记录：会自动计算 totalAmount（unitPrice * quantity），
	 * 若 receiveTime 为空则设置为当前时间。
	 *
	 * @param record 要创建的 SupplyRecord 实体
	 * @return 创建是否成功
	 */
	@Override
	public boolean create(SupplyRecord record) {
		// 计算总额：若传入单价与数量，则用 BigDecimal 计算乘积并设置 totalAmount
		if (record.getUnitPrice() != null && record.getQuantity() != null) {
			record.setTotalAmount(record.getUnitPrice().multiply(new BigDecimal(record.getQuantity())));
		}
		// 如果未指定到货时间，使用当前时间
		if (record.getReceiveTime() == null) {
			record.setReceiveTime(LocalDateTime.now());
		}
		// 调用 Mapper 插入并返回是否成功
		return supplyRecordMapper.insert(record) > 0;
	}

	/**
	 * 更新供货记录：在保存前重新计算 totalAmount，防止前端绕过计算逻辑
	 *
	 * @param record 要更新的 SupplyRecord（包含 id）
	 * @return 更新是否成功
	 */
	@Override
	public boolean update(SupplyRecord record) {
		// 更新时也需重新计算 totalAmount，避免前端绕过计算
		if (record.getUnitPrice() != null && record.getQuantity() != null) {
			record.setTotalAmount(record.getUnitPrice().multiply(new BigDecimal(record.getQuantity())));
		}
		return supplyRecordMapper.update(record) > 0;
	}

	/**
	 * 根据 ID 删除供货记录
	 *
	 * @param id 要删除的记录 ID
	 * @return 删除是否成功
	 */
	@Override
	public boolean deleteById(Long id) {
		return supplyRecordMapper.deleteById(id) > 0;
	}
}


