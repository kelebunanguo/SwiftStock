package com.swiftstock.mapper;

import com.swiftstock.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
/**
 * 商品分类Mapper
 */
@Mapper
public interface CategoryMapper {
    /**
     * 根据 ID 查询分类
     *
     * @param id 分类 ID
     * @return 分类或 null
     */
    Category findById(Long id);

    /**
     * 查询所有分类（扁平列表）
     *
     * @return 分类列表
     */
    List<Category> findAll();

    /**
     * 查询所有分类并构造成树结构（含子节点）
     *
     * @return 分类树的扁平结果（Mapper/Service 层负责构造父子关系）
     */
    List<Category> findAllTree();

    /**
     * 根据父分类 ID 查询子分类
     *
     * @param parentId 父分类 ID
     * @return 子分类列表
     */
    List<Category> findByParentId(Long parentId);

    /**
     * 查询根分类（parentId 为 null 或 0）
     *
     * @return 根分类列表
     */
    List<Category> findRootCategories();

    /**
     * 查询叶子分类（无子分类）
     *
     * @return 叶子分类列表
     */
    List<Category> findLeafCategories();

    /**
     * 插入分类
     *
     * @param category 分类实体
     * @return 受影响的行数
     */
    int insert(Category category);

    /**
     * 更新分类
     *
     * @param category 分类实体（根据 id 更新）
     * @return 受影响的行数
     */
    int update(Category category);

    /**
     * 删除分类
     *
     * @param id 分类 ID
     * @return 受影响的行数
     */
    int delete(Long id);

    /**
     * 判断分类是否存在子分类
     *
     * @param id 分类 ID
     * @return 子分类数量（>0 则有子分类）
     */
    int hasChildren(Long id);

    /**
     * 判断分类下是否存在商品
     *
     * @param id 分类 ID
     * @return 关联的商品数量（>0 则有关联商品）
     */
    int hasProducts(Long id);
} 