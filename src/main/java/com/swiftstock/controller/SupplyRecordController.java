package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.dto.SupplyRecordDTO;
import com.swiftstock.entity.SupplyRecord;
import com.swiftstock.service.SupplyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 控制器：供货记录（SupplyRecord）CRUD 操作
 * <p>
 * 提供分页查询、创建、更新、删除接口，统一返回 {@link com.swiftstock.dto.Result} 结构。
 * 路径：/supply-records
 * </p>
 */
@RestController
@RequestMapping("/supply-records")
public class SupplyRecordController {

	@Autowired
	private SupplyRecordService supplyRecordService;

	/**
	 * 分页查询指定供应商的供货记录。
	 *
	 * @param supplierId 供应商 ID（必传）
	 * @param page       页码（默认 1）
	 * @param size       每页大小（默认 10）
	 * @return 包含 list 和 total 的统一 Result
	 */
	@GetMapping
	public ResponseEntity<Result<Map<String, Object>>> list(
			@RequestParam Long supplierId,
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		try {
			int p = Math.max(1, page);
			int s = Math.max(1, size);
			int offset = (p - 1) * s;
			List<SupplyRecord> list = supplyRecordService.findPage(supplierId, offset, s);
			long total = supplyRecordService.countBySupplierId(supplierId);
			Map<String, Object> data = new HashMap<>();
			data.put("list", list);
			data.put("total", total);
			return ResponseEntity.ok(Result.ok(data));
		} catch (Exception e) {
			return ResponseEntity.ok(Result.fail("查询供货记录失败：" + e.getMessage()));
		}
	}

	/**
	 * 创建供货记录；后端会计算 totalAmount，receiveTime 若为空则默认当前时间。
	 *
	 * @param record 供货记录实体（request body）
	 * @return 操作结果（success:true 表示成功）
	 */
	@PostMapping
	public ResponseEntity<Result<Boolean>> create(@RequestBody SupplyRecordDTO dto) {
		try {
			SupplyRecord record = new SupplyRecord();
			record.setSupplierId(dto.getSupplierId());
			record.setProductId(dto.getProductId());
			record.setProductName(dto.getProductName());
			record.setQuantity(dto.getQuantity());
			record.setUnitPrice(dto.getUnitPrice());
			record.setRemark(dto.getRemark());

			// parse receiveTime if provided (expected format: yyyy-MM-dd HH:mm:ss)
			if (dto.getReceiveTime() != null && !dto.getReceiveTime().trim().isEmpty()) {
				try {
					DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime rt = LocalDateTime.parse(dto.getReceiveTime(), fmt);
					record.setReceiveTime(rt);
				} catch (Exception pe) {
					return ResponseEntity.ok(Result.fail("接收时间格式错误，期望 yyyy-MM-dd HH:mm:ss"));
				}
			}

			boolean ok = supplyRecordService.create(record);
			if (ok) {
				return ResponseEntity.ok(Result.ok(true));
			} else {
				return ResponseEntity.ok(Result.fail("创建失败"));
			}
		} catch (Exception e) {
			return ResponseEntity.ok(Result.fail("创建供货记录失败：" + e.getMessage()));
		}
	}

	/**
	 * 更新供货记录（根据路径 id）。
	 *
	 * @param id     要更新的记录 ID
	 * @param record 更新后的实体内容（request body）
	 * @return 操作结果
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Result<Boolean>> update(@PathVariable Long id, @RequestBody SupplyRecordDTO dto) {
		try {
			SupplyRecord record = new SupplyRecord();
			record.setId(id);
			record.setSupplierId(dto.getSupplierId());
			record.setProductId(dto.getProductId());
			record.setProductName(dto.getProductName());
			record.setQuantity(dto.getQuantity());
			record.setUnitPrice(dto.getUnitPrice());
			record.setRemark(dto.getRemark());

			// parse receiveTime if provided
			if (dto.getReceiveTime() != null && !dto.getReceiveTime().trim().isEmpty()) {
				try {
					DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime rt = LocalDateTime.parse(dto.getReceiveTime(), fmt);
					record.setReceiveTime(rt);
				} catch (Exception pe) {
					return ResponseEntity.ok(Result.fail("接收时间格式错误，期望 yyyy-MM-dd HH:mm:ss"));
				}
			}

			boolean ok = supplyRecordService.update(record);
			if (ok) {
				return ResponseEntity.ok(Result.ok(true));
			} else {
				return ResponseEntity.ok(Result.fail("更新失败"));
			}
		} catch (Exception e) {
			return ResponseEntity.ok(Result.fail("更新供货记录失败：" + e.getMessage()));
		}
	}

	/**
	 * 删除供货记录。
	 *
	 * @param id 记录 ID
	 * @return 操作结果
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Result<Boolean>> delete(@PathVariable Long id) {
		try {
			boolean ok = supplyRecordService.deleteById(id);
			if (ok) {
				return ResponseEntity.ok(Result.ok(true));
			} else {
				return ResponseEntity.ok(Result.fail("删除失败"));
			}
		} catch (Exception e) {
			return ResponseEntity.ok(Result.fail("删除供货记录失败：" + e.getMessage()));
		}
	}
}


