package com.swiftstock.service;

/**
 * AI 商品详情生成服务接口
 */
public interface AiProductService {
	/**
	 * 生成商品详情
	 *
	 * @param productName 商品名称
	 * @param supplier 供应商
	 * @param category 商品分类
	 * @return 商品详情
	 */
	String generateProductDetail(String productName, String supplier, String category);
}


