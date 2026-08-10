package com.mirkamolcode.service;

import com.mirkamolcode.dto.AiTicketResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class HuggingFaceService {
    private final ChatClient chatClient;

    public HuggingFaceService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    public AiTicketResponse analyzeMessage(String content) {
        try {
            return chatClient.prompt()
                    .user(content)
                    .call()
                    .entity(AiTicketResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to analyze Ai response",
                    e
            );
        }
    }
}