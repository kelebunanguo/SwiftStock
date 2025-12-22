package com.swiftstock.service;

/**
 * AI 商品详情生成服务接口
 */
public interface AiProductService {
	String generateProductDetail(String productName, String supplier, String category);
}


