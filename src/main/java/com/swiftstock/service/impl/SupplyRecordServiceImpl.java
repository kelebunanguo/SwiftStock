package com.swiftstock.service.impl;

import com.swiftstock.entity.SupplyRecord;
import com.swiftstock.mapper.SupplyRecordMapper;
import com.swiftstock.service.SupplyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupplyRecordServiceImpl implements SupplyRecordService {

	@Autowired
	private SupplyRecordMapper supplyRecordMapper;

	@Override
	public SupplyRecord findById(Long id) {
		return supplyRecordMapper.findById(id);
	}

	@Override
	public List<SupplyRecord> findPage(Long supplierId, Integer offset, Integer size) {
		return supplyRecordMapper.findPage(supplierId, offset, size);
	}

	@Override
	public long countBySupplierId(Long supplierId) {
		return supplyRecordMapper.countBySupplierId(supplierId);
	}

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

	@Override
	public boolean update(SupplyRecord record) {
		// 更新时也需重新计算 totalAmount，避免前端绕过计算
		if (record.getUnitPrice() != null && record.getQuantity() != null) {
			record.setTotalAmount(record.getUnitPrice().multiply(new BigDecimal(record.getQuantity())));
		}
		return supplyRecordMapper.update(record) > 0;
	}

	@Override
	public boolean deleteById(Long id) {
		return supplyRecordMapper.deleteById(id) > 0;
	}
}


