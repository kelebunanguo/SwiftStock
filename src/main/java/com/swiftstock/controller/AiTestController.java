package com.swiftstock.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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