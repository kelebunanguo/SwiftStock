package com.swiftstock.service.impl;

import com.swiftstock.entity.Supplier;
import com.swiftstock.mapper.SupplierMapper;
import com.swiftstock.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierMapper supplierMapper;
    
    @Autowired
    private com.swiftstock.mapper.SupplyRecordMapper supplyRecordMapper;

    @Override
    public Supplier findById(Long id) {
        return supplierMapper.findById(id);
    }

    @Override
    public List<Supplier> findAll() {
        return supplierMapper.findAll();
    }

    @Override
    public List<Supplier> findPage(String name, String contactPerson, Integer offset, Integer size) {
        return supplierMapper.findPage(name, contactPerson, offset, size);
    }

    @Override
    public long count(String name, String contactPerson) {
        return supplierMapper.count(name, contactPerson);
    }

    @Override
    public boolean create(Supplier supplier) {
        return supplierMapper.insert(supplier) > 0;
    }

    @Override
    public boolean update(Supplier supplier) {
        return supplierMapper.update(supplier) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return supplierMapper.deleteById(id) > 0;
    }

    public java.util.List<com.swiftstock.entity.SupplyRecord> findRecords(Long supplierId) {
        return supplyRecordMapper.findBySupplierId(supplierId);
    }
}


