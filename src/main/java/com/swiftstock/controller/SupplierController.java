package com.swiftstock.controller;

import com.swiftstock.entity.Supplier;
import com.swiftstock.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Map<String, Object> resp = new HashMap<>();
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

        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        Supplier s = supplierService.findById(id);
        resp.put("success", true);
        resp.put("data", s);
        return ResponseEntity.ok(resp);
    }

    /**
     * 返回全部供应商
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> listAll() {
        Map<String, Object> resp = new HashMap<>();
        java.util.List<Supplier> list = supplierService.findAll();
        resp.put("success", true);
        resp.put("data", list);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}/records")
    public ResponseEntity<Map<String, Object>> getRecords(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        java.util.List<com.swiftstock.entity.SupplyRecord> records = supplierService instanceof com.swiftstock.service.impl.SupplierServiceImpl
                ? ((com.swiftstock.service.impl.SupplierServiceImpl) supplierService).findRecords(id)
                : java.util.Collections.emptyList();
        resp.put("success", true);
        resp.put("data", records);
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Supplier supplier) {
        Map<String, Object> resp = new HashMap<>();
        boolean ok = supplierService.create(supplier);
        resp.put("success", ok);
        resp.put("message", ok ? "创建成功" : "创建失败");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        Map<String, Object> resp = new HashMap<>();
        supplier.setId(id);
        boolean ok = supplierService.update(supplier);
        resp.put("success", ok);
        resp.put("message", ok ? "更新成功" : "更新失败");
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        boolean ok = supplierService.deleteById(id);
        resp.put("success", ok);
        resp.put("message", ok ? "删除成功" : "删除失败");
        return ResponseEntity.ok(resp);
    }
}


