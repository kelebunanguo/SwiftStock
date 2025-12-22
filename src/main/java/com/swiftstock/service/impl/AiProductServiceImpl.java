package com.swiftstock.service.impl;

import com.swiftstock.service.AiProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI 商品详情生成服务实现
 */
@Slf4j
@Service
public class AiProductServiceImpl implements AiProductService {

    private final ChatClient chatClient;

    @Autowired
    public AiProductServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String generateProductDetail(String productName, String supplier, String category) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("productName 不能为空");
        }

        String supplierSafe = supplier == null ? "" : supplier;
        String categorySafe = category == null ? "" : category;

        String prompt = ""
                + "你是一名专业的电商商品详情文案编辑。请根据以下信息，为商品生成一段详细、吸引人的详情描述，适合放在商品详情页。\n\n"
                + "商品名称：" + productName + "\n"
                + "供应商：" + supplierSafe + "\n"
                + "分类：" + categorySafe + "\n\n"
                + "要求：\n"
                + "- 长度 100-200 字\n"
                + "- 内容包括：品牌介绍、材质特点、设计亮点、功能卖点、适用场景、穿着体验、洗护建议等\n"
                + "- 语言自然流畅、专业有营销感，使用“轻薄透气”“速干科技”“百搭经典”“高端工艺”等词汇\n"
                + "- 适合运动服装鞋帽类商品\n"
                + "- 输出纯文本，不要任何 markdown、标题或额外说明\n\n"
                + "请直接输出商品详情文案：";

        try {
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                return "AI生成失败，请手动填写";
            }

            return aiResponse.trim();
        } catch (Exception e) {
            log.error("调用 AI 生成商品详情失败", e);
            return "AI生成失败，请手动填写";
        }
    }
}


