package com.system.chatbot.controller;

import com.system.chatbot.dto.ChatRequestDto;
import com.system.chatbot.dto.ChatResponseDto;
import com.system.chatbot.service.IChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final IChatService iChatService;

    @Value("${app.internal.secret}")
    private String expectedSecret;

    @PostMapping(path = {"/chat"},produces = {"application/json"})
    public ResponseEntity<ChatResponseDto> chat(@RequestHeader(value="X-Internal-Secret",required = false) String secretHeader,
            @RequestBody @Valid ChatRequestDto chatRequestDto){
        boolean isAuthorized = expectedSecret != null &&
                secretHeader != null &&
                MessageDigest.isEqual(expectedSecret.getBytes(), secretHeader.getBytes());

        if (!isAuthorized) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String aiReply = iChatService.getAiResponse(chatRequestDto);
        return ResponseEntity.ok(new ChatResponseDto(aiReply));
    }

}
