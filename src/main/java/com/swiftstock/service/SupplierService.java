package com.swiftstock.service;

import com.swiftstock.entity.Supplier;
import java.util.List;

/**
 * 供应商服务接口
 * - 提供对供应商的增删改查等操作接口，用于控制层注入和业务层实现解耦。
 */
 
public interface SupplierService {
    Supplier findById(Long id);
    List<Supplier> findAll();
    List<Supplier> findPage(String name, String contactPerson, Integer offset, Integer size);
    long count(String name, String contactPerson);
    boolean create(Supplier supplier);
    boolean update(Supplier supplier);
    boolean deleteById(Long id);
}


