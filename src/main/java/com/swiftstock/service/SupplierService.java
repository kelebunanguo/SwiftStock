package com.swiftstock.service;

import com.swiftstock.entity.Supplier;
import java.util.List;

public interface SupplierService {
    Supplier findById(Long id);
    List<Supplier> findAll();
    List<Supplier> findPage(String name, String contactPerson, Integer offset, Integer size);
    long count(String name, String contactPerson);
    boolean create(Supplier supplier);
    boolean update(Supplier supplier);
    boolean deleteById(Long id);
}


