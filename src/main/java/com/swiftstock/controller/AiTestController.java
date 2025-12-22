package com.swiftstock.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 该接口用于快速验证 ChatClient 是否可用，仅在受控的测试/开发环境中启用。
 */
@RestController
@RequestMapping("/api/test")
public class AiTestController {

    private final ChatClient chatClient;

    public AiTestController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai/hello")
    public Map<String, String> testAi() {
        String response = chatClient.prompt()
                .user("你好，用中文自我介绍一下")
                .call()
                .content();

        Map<String, String> result = new HashMap<>();
        result.put("code", "200");
        result.put("message", "success");
        result.put("data", response);
        return result;
    }
}