package com.mirkamolcode.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class ChatClientConfig {

    @Value("classpath:/prompt/system-lead-prompt.txt")
    private Resource systemLeadPrompt;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        String systemPrompt;
        try {
            systemPrompt = systemLeadPrompt.getContentAsString(StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return builder
                .defaultSystem(systemPrompt)
                .build();
    }
}
