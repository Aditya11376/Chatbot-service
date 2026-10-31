package com.system.chatbot.service.impl;

import com.system.chatbot.dto.*;
import com.system.chatbot.service.IChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
@Validated
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    private final RestClient geminiRestClient;

    @Override
    public String getAiResponse(@Valid ChatRequestDto chatRequestDto) {

        StringBuilder fullPrompt = new StringBuilder();

        if (chatRequestDto.systemInstruction() != null && !chatRequestDto.systemInstruction().isBlank()) {
            fullPrompt.append("System Instruction: ").append(chatRequestDto.systemInstruction()).append("\n\n");
        }

        if (chatRequestDto.locationDto() != null
                && chatRequestDto.locationDto().latitude() != null
                && chatRequestDto.locationDto().longitude() != null) {
            fullPrompt.append("[CRITICAL LIVE CONTEXT] User's Current GPS Location: Latitude ")
                    .append(chatRequestDto.locationDto().latitude())
                    .append(", Longitude ")
                    .append(chatRequestDto.locationDto().longitude())
                    .append(". (Use these exact coordinates as the user's current location for any location-based queries, ignoring any conflicting locations mentioned in the history).\n\n");
        }

        if (chatRequestDto.history() != null && !chatRequestDto.history().isEmpty()) {
            fullPrompt.append("Conversation History:\n");
            for (Map<String, String> entry : chatRequestDto.history()) {
                String sender = entry.get("sender");
                String text = entry.get("text");
                if (text != null && !text.isBlank()) {
                    fullPrompt.append(sender).append(": ").append(text).append("\n");
                }
            }
            fullPrompt.append("\n");
        }

        fullPrompt.append("User: ").append(chatRequestDto.message());

        Map<String, String> requestPayload = Map.of("message", fullPrompt.toString());

        ChatResponseDto responseDto = geminiRestClient.post()
                .uri("/gemini/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestPayload)
                .retrieve()
                .body(ChatResponseDto.class);

        return responseDto != null ? responseDto.reply() : "No response from AI service";
    }
}