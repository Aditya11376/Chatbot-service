package com.system.chatbot.service;

import com.system.chatbot.dto.ChatRequestDto;
import com.system.chatbot.dto.ChatResponseDto;
import jakarta.validation.Valid;

public interface IChatService {
    String getAiResponse(@Valid ChatRequestDto chatRequestDto);
}
