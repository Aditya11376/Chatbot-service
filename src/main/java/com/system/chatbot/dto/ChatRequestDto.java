package com.system.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record ChatRequestDto (

        @NotBlank(message = "Message cannot be blank")
        String message,

        @Valid
        LocationDto locationDto,

        List<Map<String,String>> history,

        @NotBlank(message = "System instruction can not be blank")
        String systemInstruction
){
}
