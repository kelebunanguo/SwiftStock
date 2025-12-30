package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.entity.Supplier;
import com.swiftstock.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 供应商管理控制器
 *
 * <p>职责：
 * <ul>
 *   <li>提供供应商的增删改查与分页查询接口，支持按名称与联系人筛选。</li>
 *   <li>提供获取单个供应商详情与返回全部供应商的便捷接口（用于后台管理或下拉选择）。</li>
 *   <li>提供查询供应商对应供货记录的接口（若实现类提供 findRecords 扩展方法，则返回其结果）。</li>
 * </ul>
 *
 * <p>返回格式：统一使用 {success: boolean, message?: String, data?: Object} 结构，便于前端统一处理。</p>
 *
 * <p>路径：`/suppliers`</p>
 */
@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 分页查询供应商
     */

    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        int p = page != null && page > 0 ? page : 1;
        int s = size != null && size > 0 ? size : 10;
        int offset = (p - 1) * s;

        List<Supplier> list = supplierService.findPage(name, contactPerson, offset, s);
        long total = supplierService.count(name, contactPerson);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", p);
        data.put("size", s);

        return ResponseEntity.ok(Result.ok(data));
    }

    /**
     * 根据ID查询供应商
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Supplier>> getById(@PathVariable Long id) {
        Supplier s = supplierService.findById(id);
        return ResponseEntity.ok(Result.ok(s));
    }

    /**
     * 返回全部供应商
     */
    @GetMapping("/all")
    public ResponseEntity<Result<List<Supplier>>> listAll() {
        java.util.List<Supplier> list = supplierService.findAll();
        return ResponseEntity.ok(Result.ok(list));
    }

    /**
     * 根据ID查询供应商供货记录
     */
    @GetMapping("/{id}/records")
    public ResponseEntity<Result<List<com.swiftstock.entity.SupplyRecord>>> getRecords(@PathVariable Long id) {
        java.util.List<com.swiftstock.entity.SupplyRecord> records = supplierService instanceof com.swiftstock.service.impl.SupplierServiceImpl
                ? ((com.swiftstock.service.impl.SupplierServiceImpl) supplierService).findRecords(id)
                : java.util.Collections.emptyList();
        return ResponseEntity.ok(Result.ok(records));
    }

    /**
     * 创建供应商
     */
    @PostMapping
    public ResponseEntity<Result<Boolean>> create(@RequestBody Supplier supplier) {
        boolean ok = supplierService.create(supplier);
        if (ok) {
            return ResponseEntity.ok(Result.ok(true, "创建成功"));
        } else {
            return ResponseEntity.ok(Result.fail("创建失败"));
        }
    }

    /**
     * 更新供应商
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Boolean>> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        boolean ok = supplierService.update(supplier);
        if (ok) {
            return ResponseEntity.ok(Result.ok(true, "更新成功"));
        } else {
            return ResponseEntity.ok(Result.fail("更新失败"));
        }
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Boolean>> delete(@PathVariable Long id) {
        boolean ok = supplierService.deleteById(id);
        if (ok) {
            return ResponseEntity.ok(Result.ok(true, "删除成功"));
        } else {
            return ResponseEntity.ok(Result.fail("删除失败"));
        }
    }
}


