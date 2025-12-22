package com.swiftstock.service.impl;

import com.swiftstock.entity.Supplier;
import com.swiftstock.mapper.SupplierMapper;
import com.swiftstock.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 供应商服务实现类（SupplierService 的实现）
 *
 * <p>提供供应商的 CRUD 与分页查询能力，并能查询供应商对应的供货记录。</p>
 */
@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierMapper supplierMapper;
    
    @Autowired
    private com.swiftstock.mapper.SupplyRecordMapper supplyRecordMapper;

    /**
     * 根据 ID 查询供应商
     *
     * @param id 供应商 ID
     * @return 供应商实体或 null
     */
    @Override
    public Supplier findById(Long id) {
        return supplierMapper.findById(id);
    }

    /**
     * 查询所有供应商
     *
     * @return 供应商列表
     */
    @Override
    public List<Supplier> findAll() {
        return supplierMapper.findAll();
    }

    /**
     * 分页查询供应商
     *
     * @param name          名称模糊匹配（可空）
     * @param contactPerson 联系人（可空）
     * @param offset        偏移量
     * @param size          页大小
     * @return 供应商列表（分页）
     */
    @Override
    public List<Supplier> findPage(String name, String contactPerson, Integer offset, Integer size) {
        return supplierMapper.findPage(name, contactPerson, offset, size);
    }

    /**
     * 统计符合条件的供应商数量
     *
     * @param name          名称（可空）
     * @param contactPerson 联系人（可空）
     * @return 总数
     */
    @Override
    public long count(String name, String contactPerson) {
        return supplierMapper.count(name, contactPerson);
    }

    /**
     * 创建供应商
     *
     * @param supplier 要创建的供应商实体
     * @return 是否创建成功
     */
    @Override
    public boolean create(Supplier supplier) {
        return supplierMapper.insert(supplier) > 0;
    }

    /**
     * 更新供应商信息
     *
     * @param supplier 包含 ID 的供应商实体
     * @return 是否更新成功
     */
    @Override
    public boolean update(Supplier supplier) {
        return supplierMapper.update(supplier) > 0;
    }

    /**
     * 根据 ID 删除供应商
     *
     * @param id 供应商 ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteById(Long id) {
        return supplierMapper.deleteById(id) > 0;
    }

    /**
     * 查询指定供应商的供货记录（实现类扩展方法）
     *
     * @param supplierId 供应商 ID
     * @return 供货记录列表
     */
    public java.util.List<com.swiftstock.entity.SupplyRecord> findRecords(Long supplierId) {
        return supplyRecordMapper.findBySupplierId(supplierId);
    }
}


