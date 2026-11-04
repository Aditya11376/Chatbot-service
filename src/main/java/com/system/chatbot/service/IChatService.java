package com.system.chatbot.service;

import com.system.chatbot.dto.*;
import jakarta.validation.Valid;

public interface IChatService {
    String getAiResponse(@Valid ChatRequestDto chatRequestDto);
}
